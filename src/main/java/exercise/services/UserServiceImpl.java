package exercise.services;

import exercise.annotationvip.Component;

@Component
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName() {
        return "username";
    }
}
