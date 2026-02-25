package com.custom.kettle.plugins.tableoutputwithpresql;

import java.util.List;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step(
    id = "TableOutputWithPreSQL",
    name = "TableOutputWithPreSQL.Name",
    description = "TableOutputWithPreSQL.Description",
    image = "com/custom/kettle/plugins/tableoutputwithpresql/resources/tableoutputwithpresql.svg",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
    i18nPackageName = "com.custom.kettle.plugins.tableoutputwithpresql",
    documentationUrl = ""
)
public class TableOutputWithPreSQLMeta extends TableOutputMeta {

  private static final Class<?> PKG = TableOutputWithPreSQLMeta.class;

  private String preSql = "";

  public TableOutputWithPreSQLMeta() {
    super();
  }

  public String getPreSql() {
    return preSql;
  }

  public void setPreSql( String preSql ) {
    this.preSql = preSql == null ? "" : preSql;
  }

  @Override
  public void setDefault() {
    super.setDefault();
    preSql = "";
  }

  @Override
  public Object clone() {
    TableOutputWithPreSQLMeta retval = (TableOutputWithPreSQLMeta) super.clone();
    retval.preSql = this.preSql;
    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( super.getXML() );
    retval.append( "    " ).append( XMLHandler.addTagValue( "pre_sql", preSql ) );
    return retval.toString();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore )
      throws KettleXMLException {
    super.loadXML( stepnode, databases, metaStore );
    preSql = XMLHandler.getTagValue( stepnode, "pre_sql" );
    if ( preSql == null ) {
      preSql = "";
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step,
      List<DatabaseMeta> databases ) throws KettleException {
    super.readRep( rep, metaStore, id_step, databases );
    preSql = rep.getStepAttributeString( id_step, "pre_sql" );
    if ( preSql == null ) {
      preSql = "";
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation,
      ObjectId id_step ) throws KettleException {
    super.saveRep( rep, metaStore, id_transformation, id_step );
    rep.saveStepAttribute( id_transformation, id_step, "pre_sql", preSql );
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface,
      int cnr, TransMeta transMeta, Trans trans ) {
    return new TableOutputWithPreSQL( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new TableOutputWithPreSQLData();
  }

  @Override
  public String getDialogClassName() {
    return "com.custom.kettle.plugins.tableoutputwithpresql.TableOutputWithPreSQLDialog";
  }
}
