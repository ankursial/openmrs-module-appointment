/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.appointmentscheduling.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointmentscheduling.Appointment;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentStatusHistory;
import org.openmrs.module.appointmentscheduling.TimeSlot;
import org.openmrs.module.appointmentscheduling.Appointment.AppointmentStatus;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests Appointment Status History methods in the {@link $ AppointmentService}}.
 */
public class AppointmentStatusHistoryServiceTest extends BaseModuleContextSensitiveTest {
	
	private AppointmentService service;
	
	@Before
	public void before() throws Exception {
		service = Context.getService(AppointmentService.class);
		executeDataSet("standardAppointmentTestDataset.xml");
	}
	
	@Test
	@Verifies(value = "should get all appointment status histories", method = "getAllAppointmentStatusHistories()")
	public void getAllAppointmentStatusHistories_shouldGetAllAppointmentStatusHistories() throws Exception {
		List<AppointmentStatusHistory> appointmentStatusHistories = service.getAllAppointmentStatusHistories();
		assertEquals(3, appointmentStatusHistories.size());
	}
	
	@Test
	@Verifies(value = "should get correct appointment status history", method = "getAppointmentStatusHistory(Integer)")
	public void getAppointmentStatusHistory_shouldGetCorrectAppointmentStatusHistory() throws Exception {
		AppointmentStatusHistory appointmentStatusHistory = service.getAppointmentStatusHistory(1);
		assertNotNull(appointmentStatusHistory);
		assertEquals(AppointmentStatus.WAITING, appointmentStatusHistory.getStatus());
		
		appointmentStatusHistory = service.getAppointmentStatusHistory(2);
		assertNotNull(appointmentStatusHistory);
		assertEquals(AppointmentStatus.INCONSULTATION, appointmentStatusHistory.getStatus());
		
		appointmentStatusHistory = service.getAppointmentStatusHistory(3);
		assertNotNull(appointmentStatusHistory);
		assertEquals(AppointmentStatus.MISSED, appointmentStatusHistory.getStatus());
		
		appointmentStatusHistory = service.getAppointmentStatusHistory(5);
		Assert.assertNull(appointmentStatusHistory);
	}
	
	@Test
	@Verifies(value = "should get correct appointment status histories", method = "getAppointmentStatusHistories(String)")
	public void getAppointmentStatusHistories_shouldGetCorrentAppointmentStatusHistories() throws Exception {
		List<AppointmentStatusHistory> appointmentStatusHistories = service
		        .getAppointmentStatusHistories(AppointmentStatus.WAITING);
		assertNotNull(appointmentStatusHistories);
		assertEquals(1, appointmentStatusHistories.size());
		assertEquals(AppointmentStatus.WAITING, appointmentStatusHistories.get(0).getStatus());
		
		appointmentStatusHistories = service.getAppointmentStatusHistories(AppointmentStatus.MISSED);
		assertNotNull(appointmentStatusHistories);
		assertEquals(1, appointmentStatusHistories.size());
		assertEquals(AppointmentStatus.MISSED, appointmentStatusHistories.get(0).getStatus());
	}
	
	@Test
	@Verifies(value = "should save new appointment status history", method = "saveAppointmentStatusHistory(AppointmentStatusHistory)")
	public void saveAppointmentStatusHistory_shouldSaveNewAppointmentStatusHistory() throws Exception {
		AppointmentBlock appointmentBlock = service.getAppointmentBlock(1);
		TimeSlot timeSlot = new TimeSlot(appointmentBlock, new Date(), new Date());
		Appointment appointment = service.getAppointment(1);
		AppointmentStatusHistory appointmentStatusHistory = new AppointmentStatusHistory(appointment,
		        AppointmentStatus.SCHEDULED, new Date(), new Date());
		service.saveAppointmentStatusHistory(appointmentStatusHistory);
		
		List<AppointmentStatusHistory> appointmentStatusHistories = service
		        .getAppointmentStatusHistories(AppointmentStatus.SCHEDULED);
		assertEquals(1, appointmentStatusHistories.size());
		
		//Should create a new appointment status history row.
		assertEquals(4, service.getAllAppointmentStatusHistories().size());
	}
	
	@Test
	@Verifies(value = "should save edited appointment status history", method = "saveAppointmentStatusHistory(AppointmentStatusHistory)")
	public void saveAppointmentStatusHistory_shouldSaveEditedAppointmentStatusHistory() throws Exception {
		AppointmentStatusHistory appointmentStatusHistory = service.getAppointmentStatusHistory(1);
		assertNotNull(appointmentStatusHistory);
		assertEquals(AppointmentStatus.WAITING, appointmentStatusHistory.getStatus());
		
		appointmentStatusHistory.setStatus(AppointmentStatus.RESCHEDULED);
		service.saveAppointmentStatusHistory(appointmentStatusHistory);
		
		appointmentStatusHistory = service.getAppointmentStatusHistory(1);
		assertNotNull(appointmentStatusHistory);
		assertEquals(AppointmentStatus.RESCHEDULED, appointmentStatusHistory.getStatus());
		
		//Should not change the number of appointment status histories.
		assertEquals(3, service.getAllAppointmentStatusHistories().size());
	}
	
	@Test
	@Verifies(value = "Should get correct start date of current status", method = "getAppointmentCurrentStatusStartDate(Appointment) ")
	public void getAppointmentCurrentStatusStartDate_shouldGetCorrectDate() throws ParseException {
		Date startDate = service.getAppointmentCurrentStatusStartDate(null);
		Assert.assertNull(startDate);
		
		Appointment appointment = service.getAppointment(1);
		assertNotNull(appointment);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		startDate = format.parse("2005-01-01 02:00:00.0");
		assertEquals(startDate, service.getAppointmentCurrentStatusStartDate(appointment));
	}
	
	@Test
	@Verifies(value = "Should change appointment status correctly", method = "changeAppointmentStatus(Appointment, String)")
	public void changeAppointmentStatus_shouldChangeCorrectly() {
		Appointment appointment = service.getAppointment(1);
		assertNotNull(appointment);
		
		service.changeAppointmentStatus(appointment, AppointmentStatus.COMPLETED);
		//Should add new history
		assertEquals(4, service.getAllAppointmentStatusHistories().size());
		
		//Should not add new appointment
		assertEquals(4, service.getAllAppointments().size());
	}
}
