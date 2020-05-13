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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import quarano.actions.ActionItems;
import quarano.department.TrackedCase;
import quarano.department.web.ExternalTrackedCaseRepresentations;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

/**
 * @author Oliver Drotbohm
 */
@Component
@RequiredArgsConstructor
public class AnomaliesRepresentations {

	private final @NonNull MessageSourceAccessor messages;
	private final @NonNull ExternalTrackedCaseRepresentations trackedCaseRepresentations;
	private final AnomaliesLinkRelations links = new AnomaliesLinkRelations();

	CaseActionsRepresentation toRepresentation(TrackedCase trackedCase, ActionItems items) {

		var result = CaseActionsRepresentation.of(trackedCase, items, messages);
		result.add(links.getLinksFor(trackedCase.getId(), items));

		return result;
	}

	/**
	 * @param trackedCase
	 * @param items
	 * @param summary
	 * @return
	 */
	public CaseActionSummary toSummary(TrackedCase trackedCase, ActionItems items) {

		var summary = trackedCaseRepresentations.toSummary(trackedCase);
		var result = new CaseActionSummary(trackedCase, items, summary);

		result.add(links.getLinksFor(trackedCase.getId(), items));

		return result;
	}
}
