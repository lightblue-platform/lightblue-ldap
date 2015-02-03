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
import com.redhat.lightblue.common.ldap.LdapErrorCode;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
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
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
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

    @Override
    public void afterUpdateEntityInfo(Metadata m, EntityInfo ei, boolean newEntity) {
        //Do Nothing!!
    }

    @Override
    public void afterCreateNewSchema(Metadata m, EntityMetadata md) {
        //Do Nothing!!
    }

    /**
     * Ensure that dn and objectClass are on the entity.
     */
    @Override
    public void beforeCreateNewSchema(Metadata m, EntityMetadata md) {
        LdapFieldNameTranslator ldapNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        assertUniqueFieldExists(md, ldapNameTranslator.translateAttributeName(
                LdapCrudUtil.getLdapDataStore(md).getUniqueAttribute()));

        //TODO: check for array index or Path.any

        ensureDnField(md, ldapNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_DN));

        ensureObjectClassField(md,
                ldapNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_OBJECT_CLASS));

        PredefinedFields.ensurePredefinedFields(md);
    }

    /**
     * Simply asserts that the unique field is defined in the schema.
     * @throws Error
     */
    private void assertUniqueFieldExists(EntityMetadata md, Path uniqueFieldPath) {
        try{
            md.resolve(uniqueFieldPath);
        }
        catch(Error e){
            //ignore e, it only means the field does not exist
            throw Error.get(LdapErrorCode.ERR_UNDEFINED_UNIQUE_ATTRIBUTE, uniqueFieldPath.toString());
        }
    }

    /**
     * Ensures the objectClass field is present on the entity. If not, then it will added. If so, but
     * is defined incorrectly, then an {@link Error} will be thrown.
     */
    private void ensureObjectClassField(EntityMetadata md, Path objectClassFieldPath) {
        FieldTreeNode objectClassNode;
        try{
            objectClassNode = md.resolve(objectClassFieldPath);
        }
        catch(Error e){
            addFieldToParent(md, objectClassFieldPath,
                    (Field) (objectClassNode = new ArrayField(objectClassFieldPath.getLast(), new SimpleArrayElement(StringType.TYPE))));
        }
        if(!(objectClassNode instanceof ArrayField)){
            throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, objectClassNode.getFullPath().toString());
        }
        ArrayField objectClassField = (ArrayField) objectClassNode;
        if(!(objectClassField.getElement().getType() instanceof StringType)){
            throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, objectClassField.getFullPath().toString());
        }
    }

    /**
     * Ensures the dn field is present on the entity. If not, then it will added. If so, but
     * is defined incorrectly, then an {@link Error} will be thrown.
     */
    private void ensureDnField(EntityMetadata md, Path dnFieldPath) {
        FieldTreeNode dnNode;
        try{
            dnNode = md.resolve(dnFieldPath);
        }
        catch(Error e){
            addFieldToParent(md, dnFieldPath,
                    (Field)(dnNode = new SimpleField(dnFieldPath.getLast(), StringType.TYPE)));
        }
        if(!(dnNode.getType() instanceof StringType)){
            throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, dnNode.getFullPath().toString());
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

}
