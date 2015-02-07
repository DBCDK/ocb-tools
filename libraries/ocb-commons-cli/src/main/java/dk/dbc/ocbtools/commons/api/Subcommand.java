//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.api;

//-----------------------------------------------------------------------------
public @interface Subcommand {
    /**
     * Name of the subcommand that is matched against the first argument on the command line.
     */
    public String name() default "";
}
