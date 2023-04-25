/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Integration Test for {@link CrashController}.
 *
 * @author Alex Lutz
 */
// NOT Waiting https://github.com/spring-projects/spring-boot/issues/5574, int
// test!!!
@SuppressWarnings("java:S2970") // contradicts "java:S5838"
@SpringBootTest(webEnvironment = RANDOM_PORT,
		// this ("somehow", significantly) "differs" from "non-test":
		properties = { "server.error.include-message=ALWAYS",
				/* don't need these: */
				"management.endpoints.enabled-by-default=false" })
class CrashControllerIntegrationTests {

	// i think this is the "lightest" (sub package) and quickest (who wants to
	// exclude auto config: welcome!) context, that we
	// can bootstrap, see: https://spring.io/guides/gs/multi-module/
	@SpringBootApplication(exclude = {
			/*
			 * thx: https://www.baeldung.com/spring-data-disable-auto-config,
			 * alternatively "by name":
			 */
			DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
			HibernateJpaAutoConfiguration.class })
	static class TestConfiguration {

	}

	@Value(value = "${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate restTpl;

	@Test
	@SuppressWarnings("rawtypes")
	void testTriggerExceptionJson() {
		ResponseEntity<Map> resp = restTpl.getForEntity("http://localhost:" + port + "/oups", Map.class);
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode().is5xxServerError());
		assertThat(resp.getBody().containsKey("timestamp"));
		assertThat(resp.getBody().containsKey("status"));
		assertThat(resp.getBody().containsKey("error"));
		assertThat(resp.getBody()).containsEntry("message",
				"Expected: controller used to showcase what happens when an exception is thrown");
		assertThat(resp.getBody()).containsEntry("path", "/oups");
	}

	@Test
	void testTriggerExceptionHtml() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.TEXT_HTML));
		ResponseEntity<String> resp = restTpl.exchange("http://localhost:" + port + "/oups", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode().is5xxServerError());
		assertThat(resp.getBody()).isNotNull();
		// html:
		assertThat(resp.getBody()).containsSubsequence("<body>", "<h2>", "Something happened...", "</h2>", "<p>",
				"Expected:", "controller", "used", "to", "showcase", "what", "happens", "when", "an", "exception", "is",
				"thrown", "</p>", "</body>");
		// Haha:
		assertThat(resp.getBody()).doesNotContain("Whitelabel Error Page",
				"This application has no explicit mapping for");
	}

}
