/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.crud.ldap;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.common.ldap.LightblueUtil;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Implementation of {@link MetadataListener} for LDAP.
 *
 * @author dcrissman
 */
public class LdapMetadataListener implements MetadataListener{

    @Override
    public void beforeUpdateEntityInfo(Metadata m, EntityInfo ei, boolean newEntity) {
        //Do Nothing!!
    }

    /**
     * Ensure that dn and objectClass are on the entity.
     */
    @Override
    public void beforeCreateNewSchema(Metadata m, EntityMetadata md) {
        LdapFieldNameTranslator ldapNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);
        //TODO: check for array index or Path.any

        Path dnFieldPath = ldapNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_DN);
        try{
            FieldTreeNode dnNode = md.resolve(dnFieldPath);
            if((!(dnNode instanceof SimpleField)) || (!(dnNode.getType() instanceof StringType))){
                throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, dnNode.getFullPath().toString());
            }
        }
        catch(Error e){
            if(e.getErrorCode().equals(MetadataConstants.ERR_FIELD_WRONG_TYPE)){
                throw e;
            }
            addFieldToParent(md, dnFieldPath, new SimpleField(dnFieldPath.getLast(), StringType.TYPE));
        }

        Path objectClassFieldPath = ldapNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_OBJECT_CLASS);
        try{
            FieldTreeNode objectClassNode = md.resolve(objectClassFieldPath);
            if(!(objectClassNode instanceof ArrayField)){
                throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, objectClassNode.getFullPath().toString());
            }
            ArrayField objectClassField = (ArrayField) objectClassNode;
            ArrayElement arrayElement = objectClassField.getElement();
            if((!(arrayElement instanceof SimpleArrayElement)) || (!(arrayElement.getType() instanceof StringType))){
                throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, objectClassField.getFullPath().toString());
            }
        }
        catch(Error e){
            if(e.getErrorCode().equals(MetadataConstants.ERR_FIELD_WRONG_TYPE)){
                throw e;
            }
            addFieldToParent(md, objectClassFieldPath, new ArrayField(objectClassFieldPath.getLast(), new SimpleArrayElement(StringType.TYPE)));
        }

        Path objectClassCountFieldPath = objectClassFieldPath.mutableCopy().pop().push(LightblueUtil.createArrayCountFieldName(objectClassFieldPath.getLast()));
        try{
            FieldTreeNode objectClassCountNode = md.resolve(objectClassCountFieldPath);
            if((!(objectClassCountNode instanceof SimpleField)) || (!(objectClassCountNode.getType() instanceof IntegerType))){
                throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, objectClassCountNode.getFullPath().toString());
            }
        }
        catch(Error e){
            if(e.getErrorCode().equals(MetadataConstants.ERR_FIELD_WRONG_TYPE)){
                throw e;
            }
            addFieldToParent(md, objectClassCountFieldPath, new SimpleField(objectClassCountFieldPath.getLast(), IntegerType.TYPE));
        }
    }

    /**
     * Adds the <code>newField</cod> to the <code>newFieldPath</code>.
     * @param md - {@link EntityMetadata}
     * @param newFieldPath - {@link Path} to add
     * @param newField - {@link Field} to add at the <code>newFieldPath</code>
     */
    private void addFieldToParent(EntityMetadata md, Path newFieldPath, Field newField){
        Fields fields;

        if(newFieldPath.numSegments() == 1){
            fields = md.getFields();
        }
        else{
            Path parentPath = newFieldPath.prefix(-1);
            FieldTreeNode parent = md.resolve(parentPath);

            if(parent instanceof ObjectField){
                fields = ((ObjectField) parent).getFields();
            }
            else if (parent instanceof ObjectArrayElement){
                fields = ((ObjectArrayElement) parent).getFields();
            }
            else {
                throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, parentPath.toString());
            }
        }
        fields.addNew(newField);
    }

    @Override
    public void afterUpdateEntityInfo(Metadata m, EntityInfo ei, boolean newEntity) {
        //Do Nothing!!
    }

    @Override
    public void afterCreateNewSchema(Metadata m, EntityMetadata md) {
        //Do Nothing!!
    }

}
