/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.registry.collections;

import com.liferay.registry.ServiceReference;

/**
* @author Carlos Sierra Andrés
*/
public class ServiceReferenceServiceTuple<SR, TS>
	implements Comparable<ServiceReferenceServiceTuple<SR, TS>> {

	public ServiceReferenceServiceTuple(
		ServiceReference<SR> serviceReference, TS service) {

		_serviceReference = serviceReference;
		_service = service;
	}

	@Override
	public int compareTo(
		ServiceReferenceServiceTuple<SR, TS> serviceReferenceServiceTuple) {

		return _serviceReference.compareTo(
			serviceReferenceServiceTuple.getServiceReference());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof ServiceReferenceServiceTuple)) {
			return false;
		}

		ServiceReferenceServiceTuple<SR, TS> serviceReferenceServiceTuple =
			(ServiceReferenceServiceTuple<SR, TS>)obj;

		return _serviceReference.equals(
			serviceReferenceServiceTuple.getServiceReference());
	}

	public TS getService() {
		return _service;
	}

	public ServiceReference<SR> getServiceReference() {
		return _serviceReference;
	}

	@Override
	public int hashCode() {
		return _serviceReference.hashCode();
	}

	private TS _service;
	private ServiceReference<SR> _serviceReference;

}