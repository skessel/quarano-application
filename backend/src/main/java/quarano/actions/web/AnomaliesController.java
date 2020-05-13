package quarano.actions.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import quarano.account.Department;
import quarano.account.Department.DepartmentIdentifier;
import quarano.actions.ActionItemRepository;
import quarano.actions.ActionItemsManagement;
import quarano.core.web.ErrorsDto;
import quarano.core.web.LoggedIn;
import quarano.department.TrackedCase;
import quarano.department.TrackedCase.TrackedCaseIdentifier;
import quarano.department.TrackedCaseRepository;
import quarano.department.web.TrackedCaseLinkRelations;

import java.util.Comparator;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Oliver Drotbohm
 */
@RestController
@RequiredArgsConstructor
public class AnomaliesController {

	private final @NonNull ActionItemRepository items;
	private final @NonNull ActionItemsManagement actionItems;
	private final @NonNull MessageSourceAccessor messages;
	private final @NonNull TrackedCaseRepository cases;
	private final @NonNull AnomaliesRepresentations representations;

	@GetMapping(path = "/api/hd/actions/{identifier}", produces = MediaTypes.HAL_JSON_VALUE)
	public HttpEntity<?> getAnomalies(@PathVariable TrackedCaseIdentifier identifier, //
			@LoggedIn DepartmentIdentifier department) {

		var trackedCase = cases.findById(identifier) //
				.filter(it -> it.belongsTo(department)) //
				.orElse(null);

		if (trackedCase == null) {
			return ResponseEntity.notFound().build();
		}

		var id = trackedCase.getTrackedPerson().getId();

		return ResponseEntity.ok(representations.toRepresentation(trackedCase, items.findByTrackedPerson(id)));
	}

	@PutMapping("/api/hd/actions/{identifier}/resolve")
	HttpEntity<?> resolveActions(@PathVariable TrackedCaseIdentifier identifier, //
			@Valid @RequestBody ActionsReviewed payload, //
			Errors errors, //
			@LoggedIn DepartmentIdentifier department) {

		TrackedCase trackedCase = cases.findById(identifier) //
				.filter(it -> it.belongsTo(department)) //
				.orElse(null);

		if (trackedCase == null) {
			return ResponseEntity.notFound().build();
		}

		if (errors.hasErrors()) {
			return ErrorsDto.of(errors, messages).toBadRequest();
		}

		actionItems.resolveItemsFor(trackedCase, payload.getComment());

		return getAnomalies(identifier, department);
	}

	@GetMapping("/api/hd/actions")
	public RepresentationModel<?> getAllCasesByAnomalies(@LoggedIn Department department) {

		var collect = cases.findByDepartmentId(department.getId()) //
				.map(it -> representations.toSummary(it, items.findUnresolvedByCase(it))) //
				.stream() //
				.filter(CaseActionSummary::hasUnresolvedItems) //
				.sorted(Comparator.comparing(CaseActionSummary::getPriority).reversed()) //
				.collect(Collectors.toList());

		return HalModelBuilder.halModel() //
				.embed(collect, TrackedCaseLinkRelations.ANOMALIES) //
				.build();
	}
}
