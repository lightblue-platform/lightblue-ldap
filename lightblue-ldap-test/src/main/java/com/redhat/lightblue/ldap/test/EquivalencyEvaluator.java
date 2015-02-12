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
package com.redhat.lightblue.ldap.test;

/**
 * When two Objects can be equivalent for testing purposes, but the equals method was not overridden
 * or does not provide the desired functionality.
 * 
 * @author Dennis Crissman
 */
public interface EquivalencyEvaluator {

	/**
	 * Determines if the two passed in objects are equivalent as defined by the implementation.
	 * @param obj1
	 * @param obj2
	 * @return <code>true</code> if objects are evuivalent, otherwise <code>false</code>.
	 */
	boolean isEquivalent(Object obj1, Object obj2);

}
