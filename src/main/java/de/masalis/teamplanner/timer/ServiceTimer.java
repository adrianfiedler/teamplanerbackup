package de.masalis.teamplanner.timer;

import javax.ejb.Stateless;

@Stateless
public class ServiceTimer {

	/*
	@Inject
	UserService userService;
	
	@Inject
	private Logger log;

    @Resource
    private EJBContext context;
	@Schedule(hour = "*", minute = "5", second = "0")
	public void serviceTimer() {
		List<User> allUsers = userService.findAllUsers();
		log.log(Level.INFO, "Users: "+allUsers.size());
		changePasswordsToHash(allUsers);
	}
	
	private void changePasswordsToHash(List<User> users){
		try{
			int amount = 0;
			for(User user : users){
				String passwordhashAndSalt = user.getPasswort();
				String[] splits = passwordhashAndSalt.split(":");
				if(splits.length == 2 && passwordhashAndSalt.indexOf(":") == 128 && splits[0].length() == 128 && splits[1].length() == 24){
		    		// nichts machen
					// String test = CipherUtil.getHashedPassword("test", DatatypeConverter.parseBase64Binary(splits[1]));
					// log.log(Level.INFO, "same: "+(test.equals(splits[0])? "true" : "false"));
		    	} else{
		    		//passwort hashen
		    		String neuesPw = CipherUtil.createHashedPasswordWithSaltAppended(passwordhashAndSalt);
		    		user.setPasswort(neuesPw);
		    		amount++;
		    	}
			}
			log.log(Level.INFO, "updated hashes: "+amount);
		} catch(NoSuchAlgorithmException ex){
			context.setRollbackOnly();
			ex.printStackTrace();
		}
	}
	
	*/
}