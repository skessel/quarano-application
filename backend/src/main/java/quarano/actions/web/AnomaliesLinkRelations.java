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

import quarano.actions.ActionItems;
import quarano.department.TrackedCase.TrackedCaseIdentifier;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;

/**
 * @author Oliver Drotbohm
 */
public class AnomaliesLinkRelations {

	private static final LinkRelation RESOLVE_REL = LinkRelation.of("resolve");

	public Links getLinksFor(TrackedCaseIdentifier id, ActionItems items) {

		var controller = on(AnomaliesController.class);

		var links = Links.NONE //
				.and(Link.of(fromMethodCall(controller.getAnomalies(id, null)).toUriString()).withSelfRel());

		if (items.hasUnresolvedItems()) {

			@SuppressWarnings("null")
			var uriString = fromMethodCall(controller //
					.resolveActions(id, null, null, null)).toUriString();

			links = links.and(Link.of(uriString, RESOLVE_REL));
		}

		return links;
	}
}
