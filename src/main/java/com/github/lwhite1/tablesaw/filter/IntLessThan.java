package com.github.lwhite1.tablesaw.filter;

import com.github.lwhite1.tablesaw.columns.IntColumn;
import com.github.lwhite1.tablesaw.Table;
import com.github.lwhite1.tablesaw.columns.ColumnReference;
import org.roaringbitmap.RoaringBitmap;

/**
 */
public class IntLessThan extends ColumnFilter {

  private int value;

  public IntLessThan(ColumnReference reference, int value) {
    super(reference);
    this.value = value;
  }

  public RoaringBitmap apply(Table relation) {
    IntColumn intColumn = (IntColumn) relation.column(columnReference.getColumnName());
    return intColumn.isLessThan(value);
  }
}