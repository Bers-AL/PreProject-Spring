import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static void main(String[] args) {
        ApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(AppConfig.class);
        HelloWorld bean =
                (HelloWorld) applicationContext.getBean("helloworld");
        System.out.println(bean.getMessage());

        HelloWorld bean2 = (HelloWorld) applicationContext.getBean("helloworld");
        System.out.println(bean2.getMessage());

        Cat cat = (Cat) applicationContext.getBean("cat");
        System.out.println(cat.getMessage());
        Cat cat2 = (Cat) applicationContext.getBean("cat");
        System.out.println(cat.getMessage());

        System.out.println("Сравнение объектов HelloWorld: " + bean.equals(bean2));
        System.out.println("Сравнение объектов Cat: " + cat.equals(cat2));
    }
}