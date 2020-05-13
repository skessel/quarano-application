/*
 * Copyright 2020 the original author or authors.
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
package quarano.actions.web;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.*;

import quarano.department.web.TrackedCaseLinkRelations;
import quarano.department.web.TrackedCaseStatusAware;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Oliver Drotbohm
 */
@Component
class TrackedCaseSummaryProcessor implements RepresentationModelProcessor<TrackedCaseStatusAware<?>> {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public TrackedCaseStatusAware<?> process(TrackedCaseStatusAware<?> model) {

		var trackedCase = model.getTrackedCase();

		if (trackedCase.isEnrollmentCompleted()) {

			var anomalies = on(AnomaliesController.class);

			model.add(Link.of(fromMethodCall(anomalies.getAnomalies(trackedCase.getId(), null)).toUriString(),
					TrackedCaseLinkRelations.ANOMALIES));
		}

		return model;
	}
}
