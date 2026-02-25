package com.custom.kettle.plugins.tableoutputwithpresql;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutput;

public class TableOutputWithPreSQL extends TableOutput {

  public TableOutputWithPreSQL( StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    TableOutputWithPreSQLMeta meta = (TableOutputWithPreSQLMeta) smi;
    TableOutputWithPreSQLData data = (TableOutputWithPreSQLData) sdi;

    if ( !data.preSqlExecuted ) {
      data.preSqlExecuted = true;
      String preSql = environmentSubstitute( meta.getPreSql() );
      if ( !Utils.isEmpty( preSql ) ) {
        logBasic( "Executing Pre-SQL statements..." );
        try {
          data.db.execStatements( preSql );
          if ( !data.db.isAutoCommit() ) {
            data.db.commit();
          }
          logBasic( "Pre-SQL statements executed successfully." );
        } catch ( KettleDatabaseException e ) {
          logError( "Error executing Pre-SQL: " + e.getMessage() );
          setErrors( 1 );
          stopAll();
          setOutputDone();
          throw new KettleException( "Pre-SQL execution failed", e );
        }
      }
    }

    return super.processRow( smi, sdi );
  }
}
