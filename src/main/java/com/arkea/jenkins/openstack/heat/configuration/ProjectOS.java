package com.arkea.jenkins.openstack.heat.configuration;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.exception.utils.FormExceptionUtils;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.google.common.base.Strings;

/**
 * @author Credit Mutuel Arkea
 * 
 *         Copyright 2015 Credit Mutuel Arkea
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * 
 *         Class to manage Project(Tenant) with the differents
 *         characteristics to interact with OpenStack
 *
 */
public class ProjectOS implements Describable<ProjectOS> {

	private String project;
	private String url;
	private boolean checkV3;
	private String domain;
	private String user;
	private Secret password;

	@DataBoundConstructor
	public ProjectOS(String project, String url, JSONObject v3, String user,
			Secret password) {
		this.project = project;
		this.url = url;
		if (v3 != null) {
			if (v3 instanceof JSONObject) {
				this.checkV3 = true;
				this.domain = ((JSONObject) v3).getString("domain");
			}
		}
		this.user = user;
		this.password = password;

	}

	public ProjectOS(String project, String url, boolean checkV3,
			String domain, String user, Secret password) {
		this.project = project;
		this.url = url;
		this.checkV3 = checkV3;
		this.domain = domain;
		this.user = user;
		this.password = password;

	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isCheckV3() {
		return checkV3;
	}

	public void setCheckV3(boolean checkV3) {
		this.checkV3 = checkV3;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Secret getPassword() {
		return password;
	}

	public void setPassword(Secret password) {
		this.password = password;
	}

	/**
	 * Method to check the differents inputs configuration
	 * 
	 * @return true if no errors
	 * 
	 * @throws FormException
	 *             the first formfield doesn't set
	 */
	public boolean checkData() throws FormException {

		if (Strings.isNullOrEmpty(this.project)) {
			throw FormExceptionUtils.getFormException(Messages.project_label(),
					Messages.project_name());
		} else if (Strings.isNullOrEmpty(this.url)) {
			throw FormExceptionUtils.getFormException(Messages.url_label(),
					Messages.url_name());
		} else if (this.isCheckV3() && Strings.isNullOrEmpty(this.domain)) {
			throw FormExceptionUtils.getFormException(Messages.domain_label(),
					Messages.domain_name());
		} else if (Strings.isNullOrEmpty(this.user)) {
			throw FormExceptionUtils.getFormException(Messages.user_label(),
					Messages.user_name());
		} else if (Strings.isNullOrEmpty(Secret.toString(this.password))) {
			throw FormExceptionUtils.getFormException(
					Messages.password_label(), Messages.password_name());
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Descriptor<ProjectOS> getDescriptor() {
		return Jenkins.getInstance().getDescriptor(getClass());
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<ProjectOS> {
		public String getDisplayName() {
			return "Project Openstack Configuration Pojo";
		}

		/**
		 * Test the connection to OpenStack.
		 * 
		 * @param project
		 *            project to test
		 * @param url
		 *            identity url
		 * @param v3
		 *            v3 url ?
		 * @param domain
		 *            domain if v3 url
		 * @param user
		 *            user
		 * @param password
		 *            password
		 * @return FormValidation for the result
		 * @throws FormException
		 *             if the validation fails
		 */
		public FormValidation doTestConnection(
				@QueryParameter("project") String project,
				@QueryParameter("url") String url,
				@QueryParameter("v3") boolean v3,
				@QueryParameter("domain") String domain,
				@QueryParameter("user") String user,
				@QueryParameter("password") Secret password)
				throws FormException {
			try {

				StringBuilder msg = new StringBuilder();
				if (Strings.isNullOrEmpty(project)) {
					msg.append(Messages.input_filled(Messages.project_label()));
				}
				if (Strings.isNullOrEmpty(url)) {
					msg.append(Messages.input_filled(Messages.url_label()));
				}
				if (v3 == true) {
					if (Strings.isNullOrEmpty(domain)) {
						msg.append(Messages.input_filled(Messages
								.domain_label()));
					}
				}
				if (Strings.isNullOrEmpty(user)) {
					msg.append(Messages.input_filled(Messages.user_label()));
				}
				if (Strings.isNullOrEmpty(Secret.toString(password))) {
					msg.append(Messages.input_filled(Messages.password_label()));
				}
				if (msg.length() > 0) {
					return FormValidation.warning(msg.toString());
				}
				OpenStack4jClient client = new OpenStack4jClient(new ProjectOS(
						project, url, v3, domain, user, password));
				if (client.isConnectionOK()) {
					return FormValidation.ok(Messages.formValidation_success());
				} else {
					return FormValidation.error(Messages
							.formValidation_errorConnect());
				}
			} catch (Exception e) {
				return FormValidation.error(Messages
						.formValidation_errorClient()
						+ e.getMessage()
						+ ExceptionUtils.getStackTrace(e));
			}
		}
	}
}
