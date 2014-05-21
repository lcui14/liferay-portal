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

package com.liferay.portal.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.NotificationEvent;
import com.liferay.portal.service.base.NotificationEventLocalServiceBaseImpl;

/**
 * @author Lin Cui
 */
public class NotificationEventLocalServiceImpl
	extends NotificationEventLocalServiceBaseImpl {

	public NotificationEvent addNotificationEvent(
			long userId, long classNameId, long classPK, String payload,
			long timestamp, String type)
		throws PortalException, SystemException {

		long notificationEventId = counterLocalService.increment();

		NotificationEvent notificationEvent =
			notificationEventPersistence.create(notificationEventId);

		notificationEvent.setUserId(userId);
		notificationEvent.setClassNameId(classNameId);
		notificationEvent.setClassPK(classPK);
		notificationEvent.setPayload(payload);
		notificationEvent.setTimestamp(timestamp);
		notificationEvent.setType(type);

		notificationEventPersistence.update(notificationEvent);

		return notificationEvent;
	}

}