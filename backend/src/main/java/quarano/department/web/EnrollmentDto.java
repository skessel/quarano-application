package quarano.department.web;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import quarano.department.Enrollment;
import quarano.tracking.web.TrackingController;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@RequiredArgsConstructor
public class EnrollmentDto extends RepresentationModel<EnrollmentDto> {

	public static final LinkRelation ENROLLMENT = LinkRelation.of("enrollment");

	private static final LinkRelation DETAILS = LinkRelation.of("details");
	private static final LinkRelation QUESTIONNAIRE = LinkRelation.of("questionnaire");
	private static final LinkRelation ENCOUNTERS = LinkRelation.of("encounters");
	private static final LinkRelation REOPEN = LinkRelation.of("reopen");

	private final @Getter(onMethod = @__(@JsonUnwrapped)) Enrollment enrollment;

	@Override
	@SuppressWarnings("null")
	public Links getLinks() {

		var caseController = on(TrackedCaseController.class);
		var trackingController = on(TrackingController.class);

		var enrollmentUri = fromMethodCall(caseController.enrollment(null)).toUriString();
		var questionnareUri = fromMethodCall(caseController.addQuestionaire(null, null, null)).toUriString();
		var detailsUri = fromMethodCall(trackingController.enrollmentOverview(null)).toUriString();
		var encountersUri = fromMethodCall(trackingController.getEncounters(null)).toUriString();
		var reopenUri = fromMethodCall(caseController.reopenEnrollment(null)).toUriString();

		var links = Links.NONE.and(Link.of(enrollmentUri));

		if (enrollment.isComplete()) {

			return links.and(Link.of(detailsUri, DETAILS)) //
					.and(Link.of(questionnareUri, QUESTIONNAIRE)) //
					.and(Link.of(encountersUri, ENCOUNTERS)) //
					.and(Link.of(reopenUri, REOPEN));
		}

		if (enrollment.isCompletedQuestionnaire()) {

			return links.and(Link.of(detailsUri, DETAILS)) //
					.and(Link.of(questionnareUri, QUESTIONNAIRE)) //
					.and(Link.of(encountersUri, ENCOUNTERS)) //
					.and(Link.of(encountersUri, IanaLinkRelations.NEXT));
		}

		if (enrollment.isCompletedPersonalData()) {

			return links.and(Link.of(detailsUri, DETAILS)) //
					.and(Link.of(questionnareUri, QUESTIONNAIRE)) //
					.and(Link.of(questionnareUri, IanaLinkRelations.NEXT));
		}

		return links.and(Link.of(detailsUri, DETAILS)) //
				.and(Link.of(detailsUri, IanaLinkRelations.NEXT));
	}
}
