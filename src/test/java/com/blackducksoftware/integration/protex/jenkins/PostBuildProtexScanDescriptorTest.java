/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.protex.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;

import com.blackducksoftware.integration.protex.ProtexServerInfo;
import com.blackducksoftware.integration.protex.helper.TestHelper;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider.UserFacingAction;

import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PostBuildProtexScanDescriptorTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public JenkinsRule j = new JenkinsRule();


	@Test
	public void testIsApplicable() {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		assertTrue(descriptor.isApplicable(null));
	}

	@Test
	public void testGetDisplayName() {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		assertEquals(Messages.ProtexPostScan_getDisplayName(), descriptor.getDisplayName());
	}

	@Test
	public void testGetPluginVersion() {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		assertNotNull(descriptor.getPluginVersion());
		// assertTrue(descriptor.getPluginVersion().contains("1.1.0"));
	}

	@Test
	public void testNullJsonConfigure() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));
	}

	@Test
	public void testConfigureSingleProtexServerURLWithPath() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();

		final JSONObject server = new JSONObject();
		server.element("protexPostServerName", "Test Server Name");
		server.element("protexPostServerUrl", "https://www.google.com/hello/omg/thisHasAPath");
		server.element("protexPostServerTimeOut", "999");

		json.element("protexServers", server);
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));

		final List<ProtexServerInfo> servers = descriptor.getProtexServers();
		assertEquals(1, servers.size());
		assertEquals("Test Server Name", servers.get(0).getProtexPostServerName());
		assertEquals("https://www.google.com", servers.get(0).getProtexPostServerUrl());
		assertEquals("999", servers.get(0).getProtexPostServerTimeOut());
	}

	@Test
	public void testConfigureSingleProtexServer() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();

		final JSONObject server = new JSONObject();
		server.element("protexPostServerName", "Test Server Name");
		server.element("protexPostServerUrl", "https://www.google.com");
		server.element("protexPostServerTimeOut", "999");

		json.element("protexServers", server);
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));

		final List<ProtexServerInfo> servers = descriptor.getProtexServers();
		assertEquals(1, servers.size());
		assertEquals("Test Server Name", servers.get(0).getProtexPostServerName());
		assertEquals("https://www.google.com", servers.get(0).getProtexPostServerUrl());
		assertEquals("999", servers.get(0).getProtexPostServerTimeOut());
	}

	@Test
	public void testConfigureSingleProtexServerZeroTimeout() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();

		final JSONObject server = new JSONObject();
		server.element("protexPostServerName", "Test Server Name");
		server.element("protexPostServerUrl", "https://www.google.com");
		server.element("protexPostServerTimeOut", "0");

		json.element("protexServers", server);
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));

		final List<ProtexServerInfo> servers = descriptor.getProtexServers();
		assertEquals(1, servers.size());
		assertEquals("Test Server Name", servers.get(0).getProtexPostServerName());
		assertEquals("https://www.google.com", servers.get(0).getProtexPostServerUrl());
		assertEquals("300", servers.get(0).getProtexPostServerTimeOut());
	}

	@Test
	public void testConfigureMultipleProtexServersWithPathsInUrls() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();
		final JSONArray jsonArray = new JSONArray();

		final JSONObject server = new JSONObject();
		server.element("protexPostServerName", "Test Server Name");
		server.element("protexPostServerUrl", "https://www.google.com/hello/omg/thisHasAPath");
		server.element("protexPostServerTimeOut", "999");

		final JSONObject server2 = new JSONObject();
		server2.element("protexPostServerName", "Test Server Name 2");
		server2.element("protexPostServerUrl", "http://testServer2/hello/omg/thisHasAPath");
		server2.element("protexPostServerTimeOut", "666");

		jsonArray.add(server);
		jsonArray.add(server2);
		json.element("protexServers", jsonArray);
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));

		final List<ProtexServerInfo> servers = descriptor.getProtexServers();
		assertEquals(2, servers.size());
		assertEquals("Test Server Name", servers.get(0).getProtexPostServerName());
		assertEquals("https://www.google.com", servers.get(0).getProtexPostServerUrl());
		assertEquals("999", servers.get(0).getProtexPostServerTimeOut());

		assertEquals("Test Server Name 2", servers.get(1).getProtexPostServerName());
		assertEquals("http://testServer2", servers.get(1).getProtexPostServerUrl());
		assertEquals("666", servers.get(1).getProtexPostServerTimeOut());

	}

	@Test
	public void testConfigureMultipleProtexServers() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();
		final JSONArray jsonArray = new JSONArray();

		final JSONObject server = new JSONObject();
		server.element("protexPostServerName", "Test Server Name");
		server.element("protexPostServerUrl", "https://www.google.com");
		server.element("protexPostServerTimeOut", "999");

		final JSONObject server2 = new JSONObject();
		server2.element("protexPostServerName", "Test Server Name 2");
		server2.element("protexPostServerUrl", "http://testServer2");
		server2.element("protexPostServerTimeOut", "666");

		jsonArray.add(server);
		jsonArray.add(server2);
		json.element("protexServers", jsonArray);
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));

		final List<ProtexServerInfo> servers = descriptor.getProtexServers();
		assertEquals(2, servers.size());
		assertEquals("Test Server Name", servers.get(0).getProtexPostServerName());
		assertEquals("https://www.google.com", servers.get(0).getProtexPostServerUrl());
		assertEquals("999", servers.get(0).getProtexPostServerTimeOut());

		assertEquals("Test Server Name 2", servers.get(1).getProtexPostServerName());
		assertEquals("http://testServer2", servers.get(1).getProtexPostServerUrl());
		assertEquals("666", servers.get(1).getProtexPostServerTimeOut());

	}

	@Test
	public void testConfigureMultipleProtexServersZeroTimeout() throws FormException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		final JSONObject json = new JSONObject();
		final JSONArray jsonArray = new JSONArray();

		final JSONObject server = new JSONObject();
		server.element("protexPostServerName", "Test Server Name");
		server.element("protexPostServerUrl", "https://www.google.com");
		server.element("protexPostServerTimeOut", "0");

		final JSONObject server2 = new JSONObject();
		server2.element("protexPostServerName", "Test Server Name 2");
		server2.element("protexPostServerUrl", "http://testServer2");
		server2.element("protexPostServerTimeOut", "0");

		jsonArray.add(server);
		jsonArray.add(server2);
		json.element("protexServers", jsonArray);
		final StaplerRequest req = null;
		assertTrue(descriptor.configure(req, json));

		final List<ProtexServerInfo> servers = descriptor.getProtexServers();
		assertEquals(2, servers.size());
		assertEquals("Test Server Name", servers.get(0).getProtexPostServerName());
		assertEquals("https://www.google.com", servers.get(0).getProtexPostServerUrl());
		assertEquals("300", servers.get(0).getProtexPostServerTimeOut());

		assertEquals("Test Server Name 2", servers.get(1).getProtexPostServerName());
		assertEquals("http://testServer2", servers.get(1).getProtexPostServerUrl());
		assertEquals("300", servers.get(1).getProtexPostServerTimeOut());

	}

	@Test
	public void testDoFillProtexServerIdItems() {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		ListBoxModel boxModel = descriptor.doFillProtexServerIdItems();
		assertEquals(1, boxModel.size());
		assertEquals("- none -", boxModel.get(0).name);
		assertEquals("", boxModel.get(0).value);

		final ProtexServerInfo testServer = new ProtexServerInfo("Test Server", "http://Example", "666", null);
		descriptor.getProtexServers().add(testServer);
		boxModel = descriptor.doFillProtexServerIdItems();
		assertEquals(2, boxModel.size());
		assertEquals("- none -", boxModel.get(0).name);
		assertEquals("", boxModel.get(0).value);
		assertEquals("Test Server  (http://Example)", boxModel.get(1).name);
		assertNotNull(boxModel.get(1).value);

	}

	@Test
	public void testDoFillProtexPostCredentialsItems() {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();
		ListBoxModel dropDownList = descriptor.doFillProtexPostCredentialsItems();
		assertNotNull(dropDownList);
		assertEquals("- none -", dropDownList.get(0).name);
		assertEquals("", dropDownList.get(0).value);
		String credentialId = null;
		try {
			credentialId = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "TEST_PASSWORD");
		} catch (final IOException e) {
			e.printStackTrace();
			assertNull(e);
		}
		dropDownList = descriptor.doFillProtexPostCredentialsItems();
		assertEquals("- none -", dropDownList.get(0).name);
		assertEquals("", dropDownList.get(0).value);
		final String credentials = dropDownList.get(1).value;
		assertEquals(credentialId, credentials);

	}

	@Test
	public void testDoCheckProtexServerId() throws IOException, ServletException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		final ProtexServerInfo badUrlNoProtocol = new ProtexServerInfo(null, "123", null, null);
		final ProtexServerInfo badUrlEmpty = new ProtexServerInfo(null, "http:// ", null, null);
		final ProtexServerInfo badUrlBadPath = new ProtexServerInfo(null, "http://example.com/", null, null);
		final ProtexServerInfo badUrlUnreachable = new ProtexServerInfo(null, "http://HopefullyNobodyHasReservedThisReallyLongDomain.com", null, null);

		final ProtexServerInfo goodUrl = new ProtexServerInfo(null, "https://www.google.com", null, null);

		descriptor.getProtexServers().add(badUrlNoProtocol);
		descriptor.getProtexServers().add(badUrlEmpty);
		descriptor.getProtexServers().add(badUrlBadPath);
		descriptor.getProtexServers().add(badUrlUnreachable);
		descriptor.getProtexServers().add(goodUrl);

		final String noUserName = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "TEST_PASSWORD");
		final String noPassword = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "");
		final String noCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "");
		final String validCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME",
				"TEST_PASSWORD");
		final String badCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "MADEUPUSERNAME",
				"BEERTIMEPASSWORD");

		assertEquals(Messages.ProtexPostScan_getPleaseChooseProtexServer(), descriptor.doCheckProtexServerId("", "").getMessage());
		assertEquals(Messages.ProtexPostScan_getNotAValidUrl(), descriptor.doCheckProtexServerId(badUrlNoProtocol.getProtexServerId(), "").getMessage());
		assertEquals(Messages.ProtexPostScan_getNotAValidUrl(), descriptor.doCheckProtexServerId(badUrlEmpty.getProtexServerId(), "").getMessage());
		assertEquals(FormValidation.ok(), descriptor.doCheckProtexServerId(badUrlBadPath.getProtexServerId(), ""));

		final String outputString = descriptor.doCheckProtexServerId(badUrlUnreachable.getProtexServerId(), "").getMessage();
		assertTrue(outputString.contains(
				Messages.ProtexPostScan_getCanNotReachThisServer_0_("java.net.UnknownHostException: HopefullyNobodyHasReservedThisReallyLongDomain.com")));

		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexServerId(goodUrl.getProtexServerId(), ""));

		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexServerId(goodUrl.getProtexServerId(), noUserName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexServerId(goodUrl.getProtexServerId(), noPassword));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexServerId(goodUrl.getProtexServerId(), noCredentials));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexServerId(goodUrl.getProtexServerId(), validCredentials));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexServerId(goodUrl.getProtexServerId(), badCredentials));

	}

	@Test
	public void testDoCheckProtexPostCredentials() throws IOException, ServletException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		final ProtexServerInfo badUrlNoProtocol = new ProtexServerInfo(null, "123", null, null);
		final ProtexServerInfo badUrlEmpty = new ProtexServerInfo(null, "http:// ", null, null);
		final ProtexServerInfo badUrlBadPath = new ProtexServerInfo(null, "http://example.com/", null, null);
		final ProtexServerInfo badUrlUnreachable = new ProtexServerInfo(null, "http://HopefullyNobodyHasReservedThisReallyLongDomain.com", null, null);

		final ProtexServerInfo goodUrl = new ProtexServerInfo(null, "https://www.google.com", null, null);

		descriptor.getProtexServers().add(badUrlNoProtocol);
		descriptor.getProtexServers().add(badUrlEmpty);
		descriptor.getProtexServers().add(badUrlBadPath);
		descriptor.getProtexServers().add(badUrlUnreachable);
		descriptor.getProtexServers().add(goodUrl);

		final String noUserName = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "TEST_PASSWORD");
		final String noPassword = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "");
		final String noCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "");
		final String validCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME",
				"TEST_PASSWORD");

		assertEquals(Messages.ProtexPostScan_getNoCredentialsSelected(), descriptor.doCheckProtexPostCredentials("", "").getMessage());
		assertEquals(Messages.ProtexPostScan_getNoUserNameProvided(), descriptor.doCheckProtexPostCredentials("", noUserName).getMessage());
		assertEquals(Messages.ProtexPostScan_getNoPasswordProvided(), descriptor.doCheckProtexPostCredentials("", noPassword).getMessage());
		assertEquals(Messages.ProtexPostScan_getNoCredentialsSelected(), descriptor.doCheckProtexPostCredentials("", noCredentials).getMessage());
		assertEquals(FormValidation.ok(), descriptor.doCheckProtexPostCredentials(badUrlNoProtocol.getProtexServerId(), validCredentials));
		assertEquals(FormValidation.ok(), descriptor.doCheckProtexPostCredentials(badUrlEmpty.getProtexServerId(), validCredentials));
		assertEquals(FormValidation.ok(), descriptor.doCheckProtexPostCredentials(badUrlBadPath.getProtexServerId(), validCredentials));
		assertEquals(FormValidation.ok(), descriptor.doCheckProtexPostCredentials(badUrlUnreachable.getProtexServerId(), validCredentials));

	}

	@Test
	public void testDoCheckProtexPostProjectName() throws IOException, ServletException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		final ProtexServerInfo badUrlNoProtocol = new ProtexServerInfo(null, "123", null, null);
		final ProtexServerInfo badUrlEmpty = new ProtexServerInfo(null, "http:// ", "100", null);
		final ProtexServerInfo badUrlBadPath = new ProtexServerInfo(null, "http://Example", "100", null);
		final ProtexServerInfo badUrlBadLongPath = new ProtexServerInfo(null, "http://Example/Test", "100", null);

		final ProtexServerInfo goodUrl = new ProtexServerInfo(null, "https://www.google.com", "100", null);

		descriptor.getProtexServers().add(badUrlEmpty);
		descriptor.getProtexServers().add(badUrlBadPath);
		descriptor.getProtexServers().add(badUrlBadLongPath);
		descriptor.getProtexServers().add(goodUrl);

		final String noUserName = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "TEST_PASSWORD");
		final String noPassword = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "");
		final String noCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "");
		final String validCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME",
				"TEST_PASSWORD");

		assertEquals(FormValidation.ok(), descriptor.doCheckProtexPostProjectName(badUrlNoProtocol.getProtexServerId(), validCredentials, "Test Project"));

		assertEquals(Messages.ProtexPostScan_getPleaseProvideProjectName(), descriptor.doCheckProtexPostProjectName("", "", "").getMessage());
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName("", "", "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName(goodUrl.getProtexServerId(), noUserName, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName(goodUrl.getProtexServerId(), noPassword, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName(goodUrl.getProtexServerId(), noCredentials, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName(badUrlEmpty.getProtexServerId(), validCredentials, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName(badUrlBadPath.getProtexServerId(), validCredentials, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostProjectName(badUrlBadLongPath.getProtexServerId(), validCredentials, "Test Project"));

	}

	// TODO here down

	@Test
	public void testDoCheckProtexPostTemplateProjectName() throws IOException, ServletException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		final ProtexServerInfo badUrlNoProtocol = new ProtexServerInfo(null, "123", null, null);
		final ProtexServerInfo badUrlEmpty = new ProtexServerInfo(null, "http:// ", "100", null);
		final ProtexServerInfo badUrlBadPath = new ProtexServerInfo(null, "http://Example", "100", null);
		final ProtexServerInfo badUrlBadLongPath = new ProtexServerInfo(null, "http://Example/Test", "100", null);

		final ProtexServerInfo goodUrl = new ProtexServerInfo(null, "https://www.google.com", "100", null);

		descriptor.getProtexServers().add(badUrlEmpty);
		descriptor.getProtexServers().add(badUrlBadPath);
		descriptor.getProtexServers().add(badUrlBadLongPath);
		descriptor.getProtexServers().add(goodUrl);

		final String noUserName = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "TEST_PASSWORD");
		final String noPassword = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "");
		final String noCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "");


		assertEquals(Messages.ProtexPostScan_getOptionalProjectCloneName(), descriptor.doCheckProtexPostTemplateProjectName("", "", "").getMessage());
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostTemplateProjectName("", "", "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostTemplateProjectName(goodUrl.getProtexServerId(), noUserName, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostTemplateProjectName(goodUrl.getProtexServerId(), noPassword, "Test Project"));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexPostTemplateProjectName(goodUrl.getProtexServerId(), noCredentials, "Test Project"));
	}

	@Test
	public void testDoCreateProject() throws IOException, ServletException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
	IllegalBlockSizeException, BadPaddingException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		final ProtexServerInfo badUrl = new ProtexServerInfo(null, "NOTAURL", "100", null);
		final ProtexServerInfo badUrlBadPath = new ProtexServerInfo(null, "http://Example/", "100", null);

		final ProtexServerInfo goodUrl = new ProtexServerInfo(null, "https://www.google.com", "100", null);

		descriptor.getProtexServers().add(badUrl);
		descriptor.getProtexServers().add(badUrlBadPath);
		descriptor.getProtexServers().add(goodUrl);

		final String noUserName = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "TEST_PASSWORD");
		final String noPassword = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "");
		final String noCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "");
		final String validCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME",
				"TEST_PASSWORD");


		assertEquals(Messages.ProtexPostScan_getProjectCreateFailed(), descriptor.doCreateProject("", "", "", "").getMessage());
		assertEquals(Messages.ProtexPostScan_getProjectCreateFailed(),
				descriptor.doCreateProject(goodUrl.getProtexServerId(), "", "", "").getMessage());

		assertEquals(
				"java.net.MalformedURLException: no protocol: NOTAURL",
				descriptor.doCreateProject(badUrl.getProtexServerId(), validCredentials, "TEST_PROJECT_NAME", "")
				.getMessage());
		assertEquals("Do not include &#039;/&#039; in the Server Url.",
				descriptor.doCreateProject(badUrlBadPath.getProtexServerId(), validCredentials, "TEST_PROJECT_NAME", "")
				.getMessage());
		assertEquals(
				"Protex server Username was not provided.",
				descriptor.doCreateProject(goodUrl.getProtexServerId(), noUserName,
						"TEST_PROJECT_NAME", "").getMessage());

		assertEquals(
				"Did not provide a valid Protex Password.",
				descriptor.doCreateProject(goodUrl.getProtexServerId(), noPassword,
						"TEST_PROJECT_NAME", "").getMessage());

		assertEquals(
				"Protex server Username was not provided.",
				descriptor.doCreateProject(goodUrl.getProtexServerId(), noCredentials,
						"TEST_PROJECT_NAME", "").getMessage());


	}

	@Test
	public void testDoCheckProtexScanMemory() throws IOException, ServletException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexScanMemory(2.0));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexScanMemory(4.59));
		assertEquals(Messages.ProtexPostScan_getProtexScanMemoryTooLowWarning(),
				descriptor.doCheckProtexScanMemory(1.0).getMessage());
		// assertEquals(Messages.ProtexPostScan_getProtexScanMemoryTooHighWarning(),
		// descriptor.doCheckProtexScanMemory(25.5).getMessage());
		assertEquals(Messages.ProtexPostScan_getProtexScanMemoryEmpty(),
				descriptor.doCheckProtexScanMemory(null).getMessage());
	}

	@Test
	public void testDoCheckProtexReportTemplate() throws IOException, ServletException {
		final PostBuildProtexScanDescriptor descriptor = new PostBuildProtexScanDescriptor();

		final ProtexServerInfo badUrlNoProtocol = new ProtexServerInfo(null, "123", null, null);
		final ProtexServerInfo badUrlEmpty = new ProtexServerInfo(null, "http:// ", "100", null);
		final ProtexServerInfo badUrlBadPath = new ProtexServerInfo(null, "http://Example", "100", null);
		final ProtexServerInfo badUrlBadLongPath = new ProtexServerInfo(null, "http://Example/Test", "100", null);

		final ProtexServerInfo goodUrl = new ProtexServerInfo(null, "https://www.google.com", "100", null);

		descriptor.getProtexServers().add(badUrlEmpty);
		descriptor.getProtexServers().add(badUrlBadPath);
		descriptor.getProtexServers().add(badUrlBadLongPath);
		descriptor.getProtexServers().add(goodUrl);

		final String noUserName = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "TEST_PASSWORD");
		final String noPassword = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME", "");
		final String noCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "", "");
		final String validCredentials = TestHelper.addCredentialsToStore(new UserFacingAction(), "TEST_USERNAME",
				"TEST_PASSWORD");

		final String fakeReportName = "Fake Report Name ASSERTION";

		assertEquals(FormValidation.ok(), descriptor.doCheckProtexReportTemplate(badUrlNoProtocol.getProtexServerId(), validCredentials, fakeReportName));

		assertEquals(FormValidation.ok(), descriptor.doCheckProtexReportTemplate("", "", ""));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate("", "", fakeReportName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate(goodUrl.getProtexServerId(), noUserName, fakeReportName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate(goodUrl.getProtexServerId(), noPassword, fakeReportName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate(goodUrl.getProtexServerId(), noCredentials, fakeReportName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate(badUrlEmpty.getProtexServerId(), validCredentials, fakeReportName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate(badUrlBadPath.getProtexServerId(), validCredentials, fakeReportName));
		assertEquals(FormValidation.ok(),
				descriptor.doCheckProtexReportTemplate(badUrlBadLongPath.getProtexServerId(), validCredentials, fakeReportName));

	}

}
