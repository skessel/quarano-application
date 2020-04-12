package de.wevsvirushackathon.coronareport.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountService {
	

    private PasswordEncoder passwordEncoder;
	
	private AccountRepository accountRepository;
	
	private final Log logger = LogFactory.getLog(AccountService.class);

	@Autowired
	public AccountService(PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
		super();
		this.passwordEncoder = passwordEncoder;
		this.accountRepository = accountRepository;
	}
	
	/**
	 * creates a new account, encrypts the password and stores it
	 * @param username
	 * @param unencryptedPassword
	 * @param firstname
	 * @param lastename
	 * @param departmentId
	 * @param clientId
	 * @param roleType
	 * @return
	 */
	public Account createAndStoreAccount(String username, String unencryptedPassword, String firstname, String lastename, String departmentId, Long clientId, RoleType roleType) {
		
		String encryptedPassword = passwordEncoder.encode(unencryptedPassword);
		
		Account account = new Account(username, encryptedPassword, firstname, lastename, departmentId, clientId, roleType);
		account = accountRepository.save(account);
		
		logger.info("Created account for client " + clientId + " with username " + username);
		
		return account;
	}

}