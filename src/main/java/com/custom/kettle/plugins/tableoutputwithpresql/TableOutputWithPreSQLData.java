package com.custom.kettle.plugins.tableoutputwithpresql;

import org.pentaho.di.trans.steps.tableoutput.TableOutputData;

public class TableOutputWithPreSQLData extends TableOutputData {

  public boolean preSqlExecuted = false;

  public TableOutputWithPreSQLData() {
    super();
  }
}
