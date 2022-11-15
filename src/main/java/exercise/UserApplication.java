package exercise;

import exercise.client.UserAccountClientComponent;
import exercise.injector.AppContext;


public class UserApplication {
    public static void main(String[] args) {
        long beginTime = System.currentTimeMillis();
        //Как в спринге в main
        AppContext.startApplication(UserApplication.class);
        //Показывает что работает Autowired и Qualifier
        AppContext.getService(UserAccountClientComponent.class).displayUserAccount();
        //Показывает что работает PostConstructor
        AppContext.getService(UserAccountClientComponent.class).init();
        long endTime = System.currentTimeMillis();
        Long time = (endTime - beginTime) / 1000;
        //Проверка компьютера
        if (time > 10) {
            System.out.println("ALARM ALARM ALARM TURN OFF YOUR COMPUTER OR THROW IT OUT THE WINDOW BECAUSE IT IS OLD" + "  s = " + time);
        } else {
            System.out.println("second time = " + time);
        }
    }
}
