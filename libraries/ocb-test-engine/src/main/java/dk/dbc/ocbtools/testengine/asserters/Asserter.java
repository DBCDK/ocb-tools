//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.asserters;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.testengine.testcases.ValidationResult;
import dk.dbc.ocbtools.testengine.testcases.ValidationResultType;
import dk.dbc.updateservice.service.api.ValidateEntry;
import dk.dbc.updateservice.service.api.ValidateInstance;
import dk.dbc.updateservice.service.api.ValidateWarningOrErrorEnum;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.*;

//-----------------------------------------------------------------------------
/**
 * Helper class to assert validation results for equality.
 */
public class Asserter {
    public static final String VALIDATION_PREFIX_KEY = "validation";
    public static final String UPDATE_PREFIX_KEY = "update";

    public static void assertValidation( String bundleKeyPrefix, List<ValidationResult> expected, List<ValidationResult> actual ) throws IOException {
        logger.entry( expected, actual );

        try {
            ResourceBundle bundle = ResourceBundles.getBundle( Asserter.class.getPackage().getName() + ".messages" );
            String errorCountKey = "assert." + bundleKeyPrefix + ".error.count";
            String errorKey = "assert." + bundleKeyPrefix + ".error";

            if( !expected.equals( actual ) ) {
                if( expected.size() != actual.size() ) {
                    throw new AssertionError( String.format( bundle.getString( errorCountKey ), Json.encodePretty( expected ), Json.encodePretty( actual ) ) );
                }

                for( int i = 0; i < expected.size(); i++ ) {
                    if( !expected.get( i ).equals( actual.get( i ) ) ) {
                        throw new AssertionError( String.format( bundle.getString( errorKey ), i + 1, Json.encodePretty( expected.get( i ) ), Json.encodePretty( actual.get( i ) ) ) );
                    }
                }
            }
        }
        catch( RuntimeException ex ) {
            logger.debug( "RuntimeException", ex );
            throw ex;
        }
        finally {
            logger.exit();
        }
    }

    public static void assertValidation( String bundleKeyPrefix, List<ValidationResult> expected, ValidateInstance actual ) throws IOException {
        logger.entry( expected, actual );

        try {
            assertValidation( bundleKeyPrefix, expected, convertValidateInstanceToValidationResults( actual ) );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private static List<ValidationResult> convertValidateInstanceToValidationResults( ValidateInstance instance ) {
        logger.entry( instance );

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
