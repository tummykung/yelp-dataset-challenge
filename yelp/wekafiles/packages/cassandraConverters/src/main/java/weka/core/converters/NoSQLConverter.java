package weka.core.converters;

/**
 * Marker interface for a loader/saver that connects to a
 * NoSQL database. This interface exists primarily for
 * those NoSQL databases that don't have access via
 * JDBC drivers, or if they do, do not expose all (or
 * enough functionality) via the driver, in which case
 * a custom converter is necessary.
 *
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 48815 $
 */
public interface NoSQLConverter {

}
