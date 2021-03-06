/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 **/

package org.codice.ddf.ui.searchui.geocoder.geonames;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codice.ddf.spatial.geocoding.GeoEntry;
import org.codice.ddf.spatial.geocoding.GeoEntryQueryException;
import org.codice.ddf.spatial.geocoding.GeoEntryQueryable;
import org.codice.ddf.ui.searchui.geocoder.GeoResult;
import org.geotools.geometry.jts.spatialschema.geometry.DirectPositionImpl;
import org.geotools.geometry.jts.spatialschema.geometry.primitive.PointImpl;
import org.junit.Before;
import org.junit.Test;
import org.opengis.geometry.primitive.Point;

public class TestGeoNamesLocalIndex {
    private GeoNamesLocalIndex geoNamesLocalIndex;
    private GeoEntryQueryable geoEntryQueryable;

    private static final GeoEntry GEO_ENTRY_1 = new GeoEntry.Builder()
            .name("Phoenix")
            .latitude(10)
            .longitude(-20)
            .featureCode("PPL")
            .population(1000000)
            .alternateNames("")
            .build();

    private static final GeoEntry GEO_ENTRY_2 = new GeoEntry.Builder()
            .name("Tempe")
            .latitude(0)
            .longitude(-90)
            .featureCode("PPLC")
            .population(10000000)
            .alternateNames("Tempe2")
            .build();

    @Before
    public void setUp() {
        geoNamesLocalIndex = new GeoNamesLocalIndex();
        geoEntryQueryable = mock(GeoEntryQueryable.class);
        geoNamesLocalIndex.setGeoEntryQueryable(geoEntryQueryable);
    }

    @Test
    public void testWithResults() {
        final List<GeoEntry> topResults = Arrays.asList(GEO_ENTRY_1, GEO_ENTRY_2);
        doReturn(topResults).when(geoEntryQueryable).query("Phoenix", 1);

        final GeoResult geoResult = geoNamesLocalIndex.getLocation("Phoenix");
        assertThat(geoResult.getFullName(), is(equalTo(GEO_ENTRY_1.getName())));

        final Point point = new PointImpl(new DirectPositionImpl(
                GEO_ENTRY_1.getLongitude(), GEO_ENTRY_1.getLatitude()));
        assertThat(geoResult.getPoint(), is(equalTo(point)));
    }

    @Test
    public void testWithNoResults() {
        final List<GeoEntry> noResults = Collections.emptyList();
        doReturn(noResults).when(geoEntryQueryable).query("Tempe", 1);

        final GeoResult geoResult = geoNamesLocalIndex.getLocation("Tempe");
        assertThat(geoResult, is(nullValue()));
    }

    @Test
    public void testExceptionInQuery() {
        doThrow(GeoEntryQueryException.class).when(geoEntryQueryable).query("Arizona", 1);

        final GeoResult geoResult = geoNamesLocalIndex.getLocation("Arizona");
        assertThat(geoResult, is(nullValue()));
    }
}
