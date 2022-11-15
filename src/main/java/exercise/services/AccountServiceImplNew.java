package exercise.services;

import exercise.annotationvip.Component;

@Component
public class AccountServiceImplNew implements AccountService{
    @Override
    public Long getAccountNumber(String userName) {
        return 888888888L;
    }
}
