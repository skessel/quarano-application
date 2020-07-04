package quarano.account;

import org.junit.jupiter.api.Test;
import quarano.core.EmailAddress;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

	@Test
	void isPasswordChangeRequiredWithUnchangedPassword() {
		Account account = new Account("admin", Password.EncryptedPassword.of("***"), "Mark", "Muster",
				EmailAddress.of("muster@department1.de"), DepartmentDataInitializer.DEPARTMENT_ID_DEP1, Arrays.asList(),
				Optional.empty());
		assertThat(account.isPasswordChangeRequired()).isTrue();
	}

	@Test
	void isPasswordChangeRequiredWithAlreadyChangedPassword() {
		Account account = new Account("admin", Password.EncryptedPassword.of("***"), "Mark", "Muster",
				EmailAddress.of("muster@department1.de"), DepartmentDataInitializer.DEPARTMENT_ID_DEP1, Arrays.asList(),
				Optional.of(LocalDateTime.now()));
		assertThat(account.isPasswordChangeRequired()).isFalse();
	}

}
