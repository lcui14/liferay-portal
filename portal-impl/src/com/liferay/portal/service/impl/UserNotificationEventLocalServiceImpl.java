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
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.messaging.DestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackRegistryUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.model.NotificationEvent;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserNotificationDeliveryConstants;
import com.liferay.portal.model.UserNotificationEvent;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.base.UserNotificationEventLocalServiceBaseImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Edward Han
 * @author Brian Wing Shun Chan
 */
public class UserNotificationEventLocalServiceImpl
	extends UserNotificationEventLocalServiceBaseImpl {

	@Override
	public UserNotificationEvent addUserNotificationEvent(
			long userId,
			com.liferay.portal.kernel.notifications.NotificationEvent
				notificationEvent)
		throws PortalException, SystemException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setUuid(notificationEvent.getUuid());

		JSONObject jsonObject = notificationEvent.getPayload();

		long classNameId = classNameLocalService.getClassNameId(
			jsonObject.getString("className"));

		if (classNameId <= 0) {
			classNameId = classNameLocalService.getClassNameId(
				jsonObject.getString("entryClassName"));
		}

		long classPK = jsonObject.getLong("classPK");

		if (classPK <= 0) {
			classPK = jsonObject.getLong("entryClassPK");
		}

		NotificationEvent mNotificationEvent =
			notificationEventLocalService.addNotificationEvent(
				userId, classNameId, classPK, jsonObject.toString(),
				notificationEvent.getTimestamp(), notificationEvent.getType());

		return addUserNotificationEvent(
			userId, mNotificationEvent.getNotificationEventId(),
			notificationEvent.getDeliveryType(),
			notificationEvent.getDeliverBy(), notificationEvent.isArchived(),
			serviceContext);
	}

	@Override
	public UserNotificationEvent addUserNotificationEvent(
			long userId, long notificationEventId, int deliveryType)
		throws PortalException, SystemException {

		return addUserNotificationEvent(
			userId, notificationEventId, deliveryType, 0, false);
	}

	@Override
	public UserNotificationEvent addUserNotificationEvent(
			long userId, long notificationEventId, int deliveryType,
			long deliverBy, boolean archived)
		throws PortalException, SystemException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setUuid(PortalUUIDUtil.generate());

		return addUserNotificationEvent(
				userId, notificationEventId, deliveryType, deliverBy, archived,
			serviceContext);
	}

	@Override
	public UserNotificationEvent addUserNotificationEvent(
			long userId, long notificationEventId, int deliveryType,
			long deliverBy, boolean archived, ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		long userNotificationEventId = counterLocalService.increment();

		UserNotificationEvent userNotificationEvent =
			userNotificationEventPersistence.create(userNotificationEventId);

		userNotificationEvent.setUuid(serviceContext.getUuid());
		userNotificationEvent.setCompanyId(user.getCompanyId());
		userNotificationEvent.setUserId(userId);
		userNotificationEvent.setNotificationEventId(notificationEventId);
		userNotificationEvent.setDeliveryType(deliveryType);
		userNotificationEvent.setDeliverBy(deliverBy);
		userNotificationEvent.setDelivered(false);
		userNotificationEvent.setArchived(archived);

		userNotificationEventPersistence.update(userNotificationEvent);

		return userNotificationEvent;
	}

	/**
	 * @deprecated As of 7.0.0 {@link #addUserNotificationEvent(long, String,
	 *             long, int, long, String, boolean, ServiceContext)}
	 */
	@Deprecated
	@Override
	public UserNotificationEvent addUserNotificationEvent(
			long userId, long notificationEventId, long deliverBy,
			boolean archived, ServiceContext serviceContext)
		throws PortalException, SystemException {

		return addUserNotificationEvent(
			userId, notificationEventId,
			UserNotificationDeliveryConstants.TYPE_WEBSITE, deliverBy, archived,
			serviceContext);
	}

	@Override
	public List<UserNotificationEvent> addUserNotificationEvents(
			long userId, Collection<
				com.liferay.portal.kernel.notifications.NotificationEvent>
				notificationEvents)
		throws PortalException, SystemException {

		List<UserNotificationEvent> userNotificationEvents =
			new ArrayList<UserNotificationEvent>(notificationEvents.size());

		for (com.liferay.portal.kernel.notifications.NotificationEvent
			notificationEvent : notificationEvents) {

			UserNotificationEvent userNotificationEvent =
				addUserNotificationEvent(userId, notificationEvent);

			userNotificationEvents.add(userNotificationEvent);
		}

		return userNotificationEvents;
	}

	@Override
	public void deleteUserNotificationEvent(String uuid, long companyId)
		throws SystemException {

		userNotificationEventPersistence.removeByUuid_C(uuid, companyId);
	}

	@Override
	public void deleteUserNotificationEvents(
			Collection<String> uuids, long companyId)
		throws SystemException {

		for (String uuid : uuids) {
			deleteUserNotificationEvent(uuid, companyId);
		}
	}

	@Override
	public List<UserNotificationEvent> getArchivedUserNotificationEvents(
			long userId, boolean archived)
		throws SystemException {

		return userNotificationEventPersistence.findByU_A(userId, archived);
	}

	@Override
	public List<UserNotificationEvent> getArchivedUserNotificationEvents(
			long userId, boolean archived, int start, int end)
		throws SystemException {

		return userNotificationEventPersistence.findByU_A(
			userId, archived, start, end);
	}

	@Override
	public List<UserNotificationEvent> getArchivedUserNotificationEventsByType(
			long userId, int deliveryType, boolean archived)
		throws SystemException {

		return userNotificationEventPersistence.findByU_D_A(
			userId, deliveryType, archived);
	}

	@Override
	public List<UserNotificationEvent> getArchivedUserNotificationEventsByType(
			long userId, int deliveryType, boolean archived, int start, int end)
		throws SystemException {

		return userNotificationEventPersistence.findByU_D_A(
			userId, deliveryType, archived, start, end);
	}

	@Override
	public int getArchivedUserNotificationEventsCount(
			long userId, boolean archived)
		throws SystemException {

		return userNotificationEventPersistence.countByU_A(userId, archived);
	}

	@Override
	public int getArchivedUserNotificationEventsCountByType(
			long userId, int deliveryType, boolean archived)
		throws SystemException {

		return userNotificationEventPersistence.countByU_D_A(
			userId, deliveryType, archived);
	}

	@Override
	public List<UserNotificationEvent> getDeliveredUserNotificationEvents(
			long userId, boolean delivered)
		throws SystemException {

		return userNotificationEventPersistence.findByU_D(userId, delivered);
	}

	@Override
	public List<UserNotificationEvent> getDeliveredUserNotificationEvents(
			long userId, boolean delivered, int start, int end)
		throws SystemException {

		return userNotificationEventPersistence.findByU_D(
			userId, delivered, start, end);
	}

	@Override
	public List<UserNotificationEvent> getDeliveredUserNotificationEventsByType(
			long userId, int deliveryType, boolean delivered)
		throws SystemException {

		return userNotificationEventPersistence.findByU_D_D(
			userId, deliveryType, delivered);
	}

	@Override
	public List<UserNotificationEvent> getDeliveredUserNotificationEventsByType(
			long userId, int deliveryType, boolean delivered, int start,
			int end)
		throws SystemException {

		return userNotificationEventPersistence.findByU_D_D(
			userId, deliveryType, delivered, start, end);
	}

	@Override
	public int getDeliveredUserNotificationEventsCount(
			long userId, boolean delivered)
		throws SystemException {

		return userNotificationEventPersistence.countByU_D(userId, delivered);
	}

	@Override
	public int getDeliveredUserNotificationEventsCountByType(
			long userId, int deliveryType, boolean delivered)
		throws SystemException {

		return userNotificationEventPersistence.countByU_D_D(
			userId, deliveryType, delivered);
	}

	@Override
	public List<UserNotificationEvent> getUserNotificationEvents(long userId)
		throws SystemException {

		return userNotificationEventPersistence.findByUserId(userId);
	}

	/**
	 * @deprecated As of 6.2.0 {@link #getArchivedUserNotificationEvents(long,
	 *             boolean)}
	 */
	@Deprecated
	@Override
	public List<UserNotificationEvent> getUserNotificationEvents(
			long userId, boolean archived)
		throws SystemException {

		return getArchivedUserNotificationEvents(userId, archived);
	}

	/**
	 * @deprecated As of 6.2.0 {@link #getArchivedUserNotificationEvents(long,
	 *             boolean, int, int)}
	 */
	@Deprecated
	@Override
	public List<UserNotificationEvent> getUserNotificationEvents(
			long userId, boolean archived, int start, int end)
		throws SystemException {

		return getArchivedUserNotificationEvents(userId, archived, start, end);
	}

	@Override
	public List<UserNotificationEvent> getUserNotificationEvents(
			long userId, int start, int end)
		throws SystemException {

		return userNotificationEventPersistence.findByUserId(
			userId, start, end);
	}

	@Override
	public int getUserNotificationEventsCount(long userId)
		throws SystemException {

		return userNotificationEventPersistence.countByUserId(userId);
	}

	/**
	 * @deprecated As of 6.2.0 {@link
	 *             #getArchivedUserNotificationEventsCount(long, boolean)}
	 */
	@Deprecated
	@Override
	public int getUserNotificationEventsCount(long userId, boolean archived)
		throws SystemException {

		return getArchivedUserNotificationEventsCount(userId, archived);
	}

	@Override
	public UserNotificationEvent sendUserNotificationEvents(
			long userId, long notificationEventId, int deliveryType)
		throws PortalException, SystemException {

		UserNotificationEvent userNotificationEvent = addUserNotificationEvent(
			userId, notificationEventId, deliveryType);

		if (deliveryType == UserNotificationDeliveryConstants.TYPE_PUSH) {
			NotificationEvent notificationEvent =
				notificationEventLocalService.fetchNotificationEvent(
					notificationEventId);

			if (notificationEvent != null) {
				sendPushNotification(notificationEvent);
			}
		}

		return userNotificationEvent;
	}

	@Override
	public UserNotificationEvent updateUserNotificationEvent(
			String uuid, long companyId, boolean archive)
		throws SystemException {

		List<UserNotificationEvent> userNotificationEvents =
			userNotificationEventPersistence.findByUuid_C(uuid, companyId);

		if (userNotificationEvents.isEmpty()) {
			return null;
		}

		UserNotificationEvent userNotificationEvent =
			userNotificationEvents.get(0);

		userNotificationEvent.setArchived(archive);

		userNotificationEventPersistence.update(userNotificationEvent);

		return userNotificationEvent;
	}

	@Override
	public List<UserNotificationEvent> updateUserNotificationEvents(
			Collection<String> uuids, long companyId, boolean archive)
		throws SystemException {

		List<UserNotificationEvent> userNotificationEvents =
			new ArrayList<UserNotificationEvent>();

		for (String uuid : uuids) {
			userNotificationEvents.add(
				updateUserNotificationEvent(uuid, companyId, archive));
		}

		return userNotificationEvents;
	}

	protected void sendPushNotification(
		final NotificationEvent notificationEvent) {

		TransactionCommitCallbackRegistryUtil.registerCallback(
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					Message message = new Message();

					message.setPayload(notificationEvent.getPayload());

					MessageBusUtil.sendMessage(
						DestinationNames.PUSH_NOTIFICATION, message);

					return null;
				}

			});
	}

}