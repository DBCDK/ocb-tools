package dk.dbc.ocbtools.commons.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Subcommand {
    /**
     * Name of the subcommand that is matched against the first argument on the command line.
     */
    String name() default "";

    /**
     * Description of the usage of the subcommand.
     */
    String description() default "";

    /**
     * Usage description.
     */
    String usage() default "[options]";
}
