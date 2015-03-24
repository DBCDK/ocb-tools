//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.testengine.testcases.ValidationResult;
import dk.dbc.ocbtools.testengine.testcases.ValidationResultType;
import dk.dbc.updateservice.service.api.ValidateEntry;
import dk.dbc.updateservice.service.api.ValidateInstance;
import dk.dbc.updateservice.service.api.ValidateWarningOrErrorEnum;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by stp on 19/03/15.
 */
public class Asserter {
    public static void assertValidation( List<ValidationResult> expected, List<ValidationResult> actual ) throws IOException {
        logger.entry();

        try {
            if( !expected.equals( actual ) ) {
                if( expected.size() != actual.size() ) {
                    throw new AssertionError( String.format( "Number of validation errors differ.\nExpected:\n%s\nActual:\n%s\n", Json.encodePretty( expected ), Json.encodePretty( actual ) ) );
                }

                for( int i = 0; i < expected.size(); i++ ) {
                    if( !expected.get( i ).equals( actual.get( i ) ) ) {
                        throw new AssertionError( String.format( "Validation error at position %s differ.\n" +
                                "Expected:\n" +
                                "%s\n" +
                                "Actual:\n" +
                                "%s\n", i + 1, Json.encodePretty( expected.get( i ) ), Json.encodePretty( actual.get( i ) ) ) );
                    }
                }
            }
        }
        finally {
            logger.exit();
        }
    }

    public static void assertValidation( List<ValidationResult> expected, ValidateInstance actual ) throws IOException {
        logger.entry();

        try {
            assertValidation( expected, convertValidateInstanceToValidationResults( actual ) );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private static List<ValidationResult> convertValidateInstanceToValidationResults( ValidateInstance instance ) {
        logger.entry();

        List<ValidationResult> result = new ArrayList<>();
        try {
            if( instance == null ) {
                return result;
            }

            for( ValidateEntry entry : instance.getValidateEntry() ) {
                ValidationResult val = new ValidationResult();

                if( entry.getWarningOrError() == ValidateWarningOrErrorEnum.WARNING ) {
                    val.setType( ValidationResultType.WARNING );
                }
                if( entry.getWarningOrError() == ValidateWarningOrErrorEnum.ERROR ) {
                    val.setType( ValidationResultType.ERROR );
                }

                HashMap<String, Object> params = new HashMap<>();

                if( entry.getUrlForDocumentation() != null ) {
                    params.put( "url", entry.getUrlForDocumentation() );
                }
                if( entry.getMessage() != null ) {
                    params.put( "message", entry.getMessage() );
                }
                if( entry.getOrdinalPositionOfField() != null ) {
                    params.put( "fieldno", entry.getOrdinalPositionOfField().intValue() );
                }
                if( entry.getOrdinalPositionOfSubField() != null ) {
                    params.put( "subfieldno", entry.getOrdinalPositionOfSubField().intValue() );
                }
                val.setParams( params );

                result.add( val );
            }

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( Asserter.class );
}
