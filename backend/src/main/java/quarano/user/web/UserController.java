package quarano.user.web;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import quarano.account.Account;
import quarano.account.DepartmentRepository;
import quarano.actions.web.AnomaliesController;
import quarano.core.web.LoggedIn;
import quarano.core.web.MapperWrapper;
import quarano.department.TrackedCase;
import quarano.department.TrackedCaseRepository;
import quarano.department.web.EnrollmentDto;
import quarano.department.web.TrackedCaseController;
import quarano.department.web.TrackedCaseLinkRelations;
import quarano.tracking.TrackedPersonRepository;
import quarano.tracking.web.TrackedPersonDto;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

	private final @NonNull TrackedPersonRepository trackedPersonRepository;
	private final @NonNull DepartmentRepository departments;
	private final @NonNull TrackedCaseRepository cases;
	private final @NonNull MapperWrapper mapper;

	@GetMapping("/me")
	ResponseEntity<?> getMe(@LoggedIn Account account) {

		var userDto = UserDto.of(account);

		departments.findById(account.getDepartmentId()) //
				.map(it -> mapper.map(it, DepartmentDto.class)) //
				.ifPresent(userDto::setHealthDepartment);

		var person = trackedPersonRepository.findByAccount(account); //

		person.map(it -> mapper.map(it, TrackedPersonDto.class)) //
				.ifPresent(userDto::setClient);

		var caseController = on(TrackedCaseController.class);

		var enrollmentUri = fromMethodCall(caseController.enrollment(null)).toUriString();

		person.flatMap(cases::findByTrackedPerson) //
				.map(TrackedCase::getEnrollment).map(EnrollmentDto::new) //
				.ifPresent(it -> {

					userDto.add(Link.of(enrollmentUri, EnrollmentDto.ENROLLMENT));
					userDto.add(Link.of(enrollmentUri, IanaLinkRelations.NEXT));
					userDto.setEnrollment(it);
				});

		if (account.isCaseAgent()) {

			userDto.add(Link.of(fromMethodCall(caseController.getCases(null)).toUriString(), TrackedCaseLinkRelations.CASES));
			userDto.add(Link.of(fromMethodCall(on(AnomaliesController.class).getAllCasesByAnomalies(null)).toUriString(),
					TrackedCaseLinkRelations.ANOMALIES));
		}

		return ResponseEntity.ok(userDto);
	}
}
