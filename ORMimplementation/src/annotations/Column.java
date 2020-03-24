package annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Column {
	public String name();
	public String index_type() default "";
}
