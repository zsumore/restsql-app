package org.geotools.filter.text.ecql;

import org.geotools.filter.text.commons.AbstractCompilerFactory;
import org.geotools.filter.text.commons.ICompiler;
import org.opengis.filter.FilterFactory;

final class SqlECQLCompilerFactory extends AbstractCompilerFactory {

    /**
     * Creates an instance of {@link ECQLCompiler}
     */
    @Override
    protected ICompiler createCompiler(final String predicate, final FilterFactory filterFactory) {

        return new SqlECQLCompiler(predicate, filterFactory);
    }

}
