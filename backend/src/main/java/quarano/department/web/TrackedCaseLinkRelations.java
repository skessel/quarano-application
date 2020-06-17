package quarano.department.web;

import org.springframework.hateoas.LinkRelation;

/**
 * @author Oliver Drotbohm
 */
public class TrackedCaseLinkRelations {

	public static final LinkRelation START_TRACKING = LinkRelation.of("start-tracking");
	public static final LinkRelation CONCLUDE = LinkRelation.of("conclude");
	public static final LinkRelation RENEW = LinkRelation.of("renew");

	public static final LinkRelation QUESTIONNAIRE = LinkRelation.of("questionnaire");
	public static final LinkRelation CONTACTS = LinkRelation.of("contacts");
}
