package mq.restful.util;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.filter.FilterCapabilities;
import org.geotools.util.Converters;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;

@SuppressWarnings("deprecation")
public class MyFilterToSQL extends FilterToSQL {

	/**
	 * Default constructor
	 */
	public MyFilterToSQL() {
		super();

	}

	public MyFilterToSQL(Writer out) {
		super(out);
	}

	protected String dateFormatPattern = "yyyy-MM-dd HH:mm:ss";

	protected DateFormat dateFormat;

	public DateFormat getDateFormat() {
		if (null == dateFormat) {
			dateFormat = new SimpleDateFormat(dateFormatPattern);
		}
		return dateFormat;
	}

	@Override
	protected void writeLiteral(Object literal) throws IOException {
		// TODO Auto-generated method stub
		if (literal == null) {
			out.write("NULL");
		} else if (literal instanceof String ) {
			String encoding = (String) Converters.convert(literal,
					String.class, null);
			if (encoding == null) {
				// could not convert back to string, use original l value
				encoding = literal.toString();
			}

			// sigle quotes must be escaped to have a valid sql string
			String escaped = encoding.replaceAll("'", "''");
			out.write("'" + escaped + "'");
		}
		else if (literal instanceof Number || literal instanceof Boolean) {
			System.out.println(String.valueOf(literal));
			out.write(String.valueOf(literal));
		} else if (literal instanceof Date) {
			String valueDate = getDateFormat().format(literal).replaceAll("'",
					"''");
			out.write("'" + valueDate + "'");
		}else {
			// we don't know the type...just convert back to a string
			String encoding = (String) Converters.convert(literal,
					String.class, null);
			if (encoding == null) {
				// could not convert back to string, use original l value
				encoding = literal.toString();
			}

			// sigle quotes must be escaped to have a valid sql string
			String escaped = encoding.replaceAll("'", "''");
			out.write("'" + escaped + "'");
		}
	}
	
	 /**
     * Encodes an Id filter
     *
     * @param filter the
     *
     * @throws RuntimeException If there's a problem writing output
     *
     */
	@Override
    public Object visit(Id filter, Object extraData) {
        if (mapper == null) {
            throw new RuntimeException(
                "Must set a fid mapper before trying to encode FIDFilters");
        }

        Set ids = filter.getIdentifiers();
        
        //LOGGER.finer("Exporting FID=" + ids);

        // prepare column name array
        String[] colNames = new String[mapper.getColumnCount()];

        for (int i = 0; i < colNames.length; i++) {
            colNames[i] = mapper.getColumnName(i);
        }

        try {
            if (ids.size() > 1) {
                out.write("(");
            }
            for (Iterator i = ids.iterator(); i.hasNext();) {
                Identifier id = (Identifier) i.next();
                Object[] attValues = mapper.getPKAttributes(id.toString());

                out.write("(");

                for (int j = 0; j < attValues.length; j++) {
                    out.write( escapeName(colNames[j]) );
                    out.write(" = '");
                    out.write(attValues[j].toString()); //DJB: changed this to attValues[j] from attValues[i].
                    out.write("'");

                    if (j < (attValues.length - 1)) {
                        out.write(" AND ");
                    }
                }

                out.write(")");

                if (i.hasNext()) {
                    out.write(" OR ");
                }
            }
            if (ids.size() > 1) {
                out.write(")");
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(IO_ERROR, e);
        }
        

        return extraData;
    }

	@Override
	protected FilterCapabilities createFilterCapabilities() {
		 FilterCapabilities capabilities = new FilterCapabilities();

	        capabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);
	        capabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
	        capabilities.addType(PropertyIsNull.class);
	        capabilities.addType(PropertyIsBetween.class);
	        capabilities.addType(Id.class);
	        capabilities.addType(IncludeFilter.class);
	        capabilities.addType(ExcludeFilter.class);
	        capabilities.addType(PropertyIsLike.class);
	        
	        //temporal filters
	        capabilities.addType(After.class);
	        capabilities.addType(Before.class);
	        capabilities.addType(Begins.class);
	        capabilities.addType(BegunBy.class);
	        capabilities.addType(During.class);
	        capabilities.addType(Ends.class);
	        capabilities.addType(EndedBy.class);
	        capabilities.addType(TContains.class);
	        capabilities.addType(TEquals.class);

	        return capabilities;
	}
	
	

}
