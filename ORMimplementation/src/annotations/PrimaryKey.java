package annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface PrimaryKey {
 public String name();
 public boolean autoincrement() default true;
 public String type() default " INTEGER ";
}
