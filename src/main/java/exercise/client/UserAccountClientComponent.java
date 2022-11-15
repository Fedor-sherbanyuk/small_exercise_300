package exercise.client;

import exercise.annotationvip.*;
import exercise.services.*;

@Component
public class UserAccountClientComponent {
    @Autowired
    private UserService userService;

    //Так можно проверить что Qualifier работает.
    @Autowired
    @Qualifier("accountServiceImpl")
    private AccountService accountService;

//    @Autowired
//    @Qualifier("accountServiceImpl_New")
//    private AccountService accountService;

    public void displayUserAccount() {
        String username = userService.getUserName();
        Long accountNumber = accountService.getAccountNumber(username);
        System.out.println("\n\tUser Name: " + username + "\n\tAccount Number: " + accountNumber);
    }

    //Просто чтобы видно, что работает PostConstructor
    @PostConstructor
    public void init(){
        System.out.println("if you see string: CREATE PostConstructor on top you have @PostConstructor");
    }
}
