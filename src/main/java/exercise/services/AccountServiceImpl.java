package exercise.services;

import exercise.annotationvip.Component;

@Component
public class AccountServiceImpl implements AccountService {
    @Override
    public Long getAccountNumber(String userName) {
        return 12345689L;
    }
}
