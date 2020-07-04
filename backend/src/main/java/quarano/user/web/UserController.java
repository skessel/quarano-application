package quarano.user.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import quarano.account.Account;
import quarano.account.AccountService;
import quarano.account.DepartmentContact.ContactType;
import quarano.account.DepartmentRepository;
import quarano.account.Password.EncryptedPassword;
import quarano.account.Password.UnencryptedPassword;
import quarano.core.web.ErrorsDto;
import quarano.core.web.LoggedIn;
import quarano.core.web.MapperWrapper;
import quarano.department.CaseType;
import quarano.department.TrackedCase;
import quarano.department.TrackedCaseRepository;
import quarano.department.web.EnrollmentDto;
import quarano.tracking.TrackedPersonRepository;
import quarano.tracking.web.TrackedPersonDto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

	private final @NonNull TrackedPersonRepository trackedPersonRepository;
	private final @NonNull DepartmentRepository departments;
	private final @NonNull TrackedCaseRepository cases;
	private final @NonNull AccountService accounts;
	private final @NonNull MapperWrapper mapper;
	private final @NonNull MessageSourceAccessor messages;

	@GetMapping("/me")
	ResponseEntity<?> getMe(@LoggedIn Account account) {

		var userDto = UserDto.of(account);

		if (account.isTrackedPerson()) {
			var person = trackedPersonRepository.findByAccount(account); //

			person.map(it -> mapper.map(it, TrackedPersonDto.class)) //
					.ifPresent(userDto::setClient);

			var trackedCase = person.flatMap(cases::findByTrackedPerson);
			trackedCase//
					.map(it -> new EnrollmentDto(it.getEnrollment())) //
					.ifPresent(userDto::setEnrollment);

			trackedCase.map(TrackedCase::getType) //
					.flatMap(type -> {

						var contactType = CaseType.INDEX == type ? ContactType.INDEX : ContactType.CONTACT;

						return departments.findById(account.getDepartmentId()) //
								.flatMap(department -> department.getContact(contactType) //
										.map(contact -> DepartmentDto.of(department, contact)));
					}) //
					.ifPresent(userDto::setHealthDepartment);
		} else {
			departments.findById(account.getDepartmentId()) //
					.map(it -> mapper.map(it, DepartmentDto.class)) //
					.ifPresent(userDto::setHealthDepartment);
		}

		return ResponseEntity.ok(userDto);
	}

	@PutMapping("/me/password")
	public HttpEntity<?> putPassword(@Valid @RequestBody NewPassword payload, Errors errors, @LoggedIn Account account) {

		return payload //
				.validate(ErrorsDto.of(errors, messages), account.getPassword(), accounts) //
				.toBadRequestOrElse(() -> {

					accounts.changePassword(UnencryptedPassword.of(payload.password), account);

					return ResponseEntity.ok().build();
				});
	}

	@Value
	static class NewPassword {

		private final @NotBlank String current, password, passwordConfirm;

		ErrorsDto validate(ErrorsDto errors, EncryptedPassword existing, AccountService accounts) {

			if (!accounts.matches(UnencryptedPassword.of(current), existing)) {
				errors.rejectField("current", "Invalid");
			}

			if (!password.equals(passwordConfirm)) {
				errors.rejectField("password", "NonMatching.password");
				errors.rejectField("passwordConfirm", "NonMatching.password");
			}

			return errors;
		}
	}
}
