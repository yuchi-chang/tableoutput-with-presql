package com.custom.kettle.plugins.tableoutputwithpresql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class TableOutputWithPreSQLDialog extends BaseStepDialog implements StepDialogInterface {

  private static final Class<?> PKG = TableOutputMeta.class; // reuse original TableOutput i18n
  private static final Class<?> MY_PKG = TableOutputWithPreSQLMeta.class; // our custom i18n

  private TableOutputWithPreSQLMeta input;

  private CTabFolder wTabFolder;
  private CTabItem wMainTab;
  private CTabItem wFieldsTab;
  private CTabItem wPreSQLTab;

  private CCombo wConnection;

  private Label wlSchema;
  private TextVar wSchema;
  private Button wbSchema;

  private Label wlTable;
  private TextVar wTable;
  private Button wbTable;

  private Label wlCommit;
  private TextVar wCommit;

  private Label wlTruncate;
  private Button wTruncate;

  private Label wlIgnore;
  private Button wIgnore;

  private Label wlSpecifyFields;
  private Button wSpecifyFields;

  private Label wlBatch;
  private Button wBatch;

  private Label wlUsePart;
  private Button wUsePart;

  private Label wlPartField;
  private ComboVar wPartField;

  private Label wlPartMonthly;
  private Button wPartMonthly;

  private Label wlPartDaily;
  private Button wPartDaily;

  private Label wlNameInField;
  private Button wNameInField;

  private Label wlNameField;
  private ComboVar wNameField;

  private Label wlNameInTable;
  private Button wNameInTable;

  private Label wlReturnKeys;
  private Button wReturnKeys;

  private Label wlReturnField;
  private TextVar wReturnField;

  private TableView wFields;

  private Button wGetFields;
  private Button wDoMapping;

  private ColumnInfo[] ciFields;

  private StyledTextComp wPreSQL;

  private Map<String, Integer> inputFields;
  private List<ColumnInfo> tableFieldColumns = new ArrayList<>();

  private boolean gotPreviousFields = false;

  public TableOutputWithPreSQLDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (TableOutputWithPreSQLMeta) in;
    inputFields = new HashMap<>();
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( MY_PKG, "TableOutputWithPreSQLDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Step name
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Connection
    wConnection = addConnectionLine( shell, wStepname, middle, margin );
    if ( input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1 ) {
      wConnection.select( 0 );
    }
    wConnection.addModifyListener( lsMod );
    wConnection.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setTableFieldCombo();
      }
    } );

    // Schema
    wlSchema = new Label( shell, SWT.RIGHT );
    wlSchema.setText( BaseMessages.getString( PKG, "TableOutputDialog.TargetSchema.Label" ) );
    props.setLook( wlSchema );
    FormData fdlSchema = new FormData();
    fdlSchema.left = new FormAttachment( 0, 0 );
    fdlSchema.right = new FormAttachment( middle, -margin );
    fdlSchema.top = new FormAttachment( wConnection, margin * 2 );
    wlSchema.setLayoutData( fdlSchema );

    wbSchema = new Button( shell, SWT.PUSH | SWT.CENTER );
    wbSchema.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    props.setLook( wbSchema );
    FormData fdbSchema = new FormData();
    fdbSchema.top = new FormAttachment( wConnection, margin * 2 );
    fdbSchema.right = new FormAttachment( 100, 0 );
    wbSchema.setLayoutData( fdbSchema );

    wSchema = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSchema );
    wSchema.addModifyListener( lsMod );
    FormData fdSchema = new FormData();
    fdSchema.left = new FormAttachment( middle, 0 );
    fdSchema.top = new FormAttachment( wConnection, margin * 2 );
    fdSchema.right = new FormAttachment( wbSchema, -margin );
    wSchema.setLayoutData( fdSchema );

    // Table
    wlTable = new Label( shell, SWT.RIGHT );
    wlTable.setText( BaseMessages.getString( PKG, "TableOutputDialog.TargetTable.Label" ) );
    props.setLook( wlTable );
    FormData fdlTable = new FormData();
    fdlTable.left = new FormAttachment( 0, 0 );
    fdlTable.right = new FormAttachment( middle, -margin );
    fdlTable.top = new FormAttachment( wbSchema, margin );
    wlTable.setLayoutData( fdlTable );

    wbTable = new Button( shell, SWT.PUSH | SWT.CENTER );
    wbTable.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    props.setLook( wbTable );
    FormData fdbTable = new FormData();
    fdbTable.right = new FormAttachment( 100, 0 );
    fdbTable.top = new FormAttachment( wbSchema, margin );
    wbTable.setLayoutData( fdbTable );

    wTable = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTable );
    wTable.addModifyListener( lsMod );
    FormData fdTable = new FormData();
    fdTable.left = new FormAttachment( middle, 0 );
    fdTable.top = new FormAttachment( wbSchema, margin );
    fdTable.right = new FormAttachment( wbTable, -margin );
    wTable.setLayoutData( fdTable );

    // Commit size
    wlCommit = new Label( shell, SWT.RIGHT );
    wlCommit.setText( BaseMessages.getString( PKG, "TableOutputDialog.CommitSize.Label" ) );
    props.setLook( wlCommit );
    FormData fdlCommit = new FormData();
    fdlCommit.left = new FormAttachment( 0, 0 );
    fdlCommit.right = new FormAttachment( middle, -margin );
    fdlCommit.top = new FormAttachment( wbTable, margin );
    wlCommit.setLayoutData( fdlCommit );

    wCommit = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wCommit );
    wCommit.addModifyListener( lsMod );
    FormData fdCommit = new FormData();
    fdCommit.left = new FormAttachment( middle, 0 );
    fdCommit.top = new FormAttachment( wbTable, margin );
    fdCommit.right = new FormAttachment( 100, 0 );
    wCommit.setLayoutData( fdCommit );

    // Truncate table
    wlTruncate = new Label( shell, SWT.RIGHT );
    wlTruncate.setText( BaseMessages.getString( PKG, "TableOutputDialog.TruncateTable.Label" ) );
    props.setLook( wlTruncate );
    FormData fdlTruncate = new FormData();
    fdlTruncate.left = new FormAttachment( 0, 0 );
    fdlTruncate.right = new FormAttachment( middle, -margin );
    fdlTruncate.top = new FormAttachment( wCommit, margin );
    wlTruncate.setLayoutData( fdlTruncate );

    wTruncate = new Button( shell, SWT.CHECK );
    props.setLook( wTruncate );
    FormData fdTruncate = new FormData();
    fdTruncate.left = new FormAttachment( middle, 0 );
    fdTruncate.top = new FormAttachment( wCommit, margin );
    fdTruncate.right = new FormAttachment( 100, 0 );
    wTruncate.setLayoutData( fdTruncate );
    wTruncate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Ignore errors
    wlIgnore = new Label( shell, SWT.RIGHT );
    wlIgnore.setText( BaseMessages.getString( PKG, "TableOutputDialog.IgnoreInsertErrors.Label" ) );
    props.setLook( wlIgnore );
    FormData fdlIgnore = new FormData();
    fdlIgnore.left = new FormAttachment( 0, 0 );
    fdlIgnore.right = new FormAttachment( middle, -margin );
    fdlIgnore.top = new FormAttachment( wTruncate, margin );
    wlIgnore.setLayoutData( fdlIgnore );

    wIgnore = new Button( shell, SWT.CHECK );
    props.setLook( wIgnore );
    FormData fdIgnore = new FormData();
    fdIgnore.left = new FormAttachment( middle, 0 );
    fdIgnore.top = new FormAttachment( wTruncate, margin );
    fdIgnore.right = new FormAttachment( 100, 0 );
    wIgnore.setLayoutData( fdIgnore );
    wIgnore.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // Specify fields
    wlSpecifyFields = new Label( shell, SWT.RIGHT );
    wlSpecifyFields.setText( BaseMessages.getString( PKG, "TableOutputDialog.SpecifyFields.Label" ) );
    props.setLook( wlSpecifyFields );
    FormData fdlSpecifyFields = new FormData();
    fdlSpecifyFields.left = new FormAttachment( 0, 0 );
    fdlSpecifyFields.right = new FormAttachment( middle, -margin );
    fdlSpecifyFields.top = new FormAttachment( wIgnore, margin );
    wlSpecifyFields.setLayoutData( fdlSpecifyFields );

    wSpecifyFields = new Button( shell, SWT.CHECK );
    props.setLook( wSpecifyFields );
    FormData fdSpecifyFields = new FormData();
    fdSpecifyFields.left = new FormAttachment( middle, 0 );
    fdSpecifyFields.top = new FormAttachment( wIgnore, margin );
    fdSpecifyFields.right = new FormAttachment( 100, 0 );
    wSpecifyFields.setLayoutData( fdSpecifyFields );
    wSpecifyFields.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // ---- Tab Folder ----
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ==== Main Tab ====
    wMainTab = new CTabItem( wTabFolder, SWT.NONE );
    wMainTab.setText( BaseMessages.getString( MY_PKG, "TableOutputWithPreSQLDialog.MainTab.Title" ) );

    Composite wMainComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wMainComp );
    FormLayout mainLayout = new FormLayout();
    mainLayout.marginWidth = 3;
    mainLayout.marginHeight = 3;
    wMainComp.setLayout( mainLayout );

    // Partitioning
    wlUsePart = new Label( wMainComp, SWT.RIGHT );
    wlUsePart.setText( BaseMessages.getString( PKG, "TableOutputDialog.UsePart.Label" ) );
    props.setLook( wlUsePart );
    FormData fdlUsePart = new FormData();
    fdlUsePart.left = new FormAttachment( 0, 0 );
    fdlUsePart.right = new FormAttachment( middle, -margin );
    fdlUsePart.top = new FormAttachment( 0, margin );
    wlUsePart.setLayoutData( fdlUsePart );

    wUsePart = new Button( wMainComp, SWT.CHECK );
    props.setLook( wUsePart );
    FormData fdUsePart = new FormData();
    fdUsePart.left = new FormAttachment( middle, 0 );
    fdUsePart.top = new FormAttachment( 0, margin );
    fdUsePart.right = new FormAttachment( 100, 0 );
    wUsePart.setLayoutData( fdUsePart );
    wUsePart.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // Partition field
    wlPartField = new Label( wMainComp, SWT.RIGHT );
    wlPartField.setText( BaseMessages.getString( PKG, "TableOutputDialog.PartField.Label" ) );
    props.setLook( wlPartField );
    FormData fdlPartField = new FormData();
    fdlPartField.left = new FormAttachment( 0, 0 );
    fdlPartField.right = new FormAttachment( middle, -margin );
    fdlPartField.top = new FormAttachment( wUsePart, margin );
    wlPartField.setLayoutData( fdlPartField );

    wPartField = new ComboVar( transMeta, wMainComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPartField );
    wPartField.addModifyListener( lsMod );
    FormData fdPartField = new FormData();
    fdPartField.left = new FormAttachment( middle, 0 );
    fdPartField.top = new FormAttachment( wUsePart, margin );
    fdPartField.right = new FormAttachment( 100, 0 );
    wPartField.setLayoutData( fdPartField );
    wPartField.addFocusListener( new FocusAdapter() {
      @Override
      public void focusGained( FocusEvent e ) {
        getFields();
      }
    } );

    // Partition monthly
    wlPartMonthly = new Label( wMainComp, SWT.RIGHT );
    wlPartMonthly.setText( BaseMessages.getString( PKG, "TableOutputDialog.PartMonthly.Label" ) );
    props.setLook( wlPartMonthly );
    FormData fdlPartMonthly = new FormData();
    fdlPartMonthly.left = new FormAttachment( 0, 0 );
    fdlPartMonthly.right = new FormAttachment( middle, -margin );
    fdlPartMonthly.top = new FormAttachment( wPartField, margin );
    wlPartMonthly.setLayoutData( fdlPartMonthly );

    wPartMonthly = new Button( wMainComp, SWT.RADIO );
    props.setLook( wPartMonthly );
    FormData fdPartMonthly = new FormData();
    fdPartMonthly.left = new FormAttachment( middle, 0 );
    fdPartMonthly.top = new FormAttachment( wPartField, margin );
    fdPartMonthly.right = new FormAttachment( 100, 0 );
    wPartMonthly.setLayoutData( fdPartMonthly );
    wPartMonthly.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Partition daily
    wlPartDaily = new Label( wMainComp, SWT.RIGHT );
    wlPartDaily.setText( BaseMessages.getString( PKG, "TableOutputDialog.PartDaily.Label" ) );
    props.setLook( wlPartDaily );
    FormData fdlPartDaily = new FormData();
    fdlPartDaily.left = new FormAttachment( 0, 0 );
    fdlPartDaily.right = new FormAttachment( middle, -margin );
    fdlPartDaily.top = new FormAttachment( wPartMonthly, margin );
    wlPartDaily.setLayoutData( fdlPartDaily );

    wPartDaily = new Button( wMainComp, SWT.RADIO );
    props.setLook( wPartDaily );
    FormData fdPartDaily = new FormData();
    fdPartDaily.left = new FormAttachment( middle, 0 );
    fdPartDaily.top = new FormAttachment( wPartMonthly, margin );
    fdPartDaily.right = new FormAttachment( 100, 0 );
    wPartDaily.setLayoutData( fdPartDaily );
    wPartDaily.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Batch update
    wlBatch = new Label( wMainComp, SWT.RIGHT );
    wlBatch.setText( BaseMessages.getString( PKG, "TableOutputDialog.Batch.Label" ) );
    props.setLook( wlBatch );
    FormData fdlBatch = new FormData();
    fdlBatch.left = new FormAttachment( 0, 0 );
    fdlBatch.right = new FormAttachment( middle, -margin );
    fdlBatch.top = new FormAttachment( wPartDaily, margin );
    wlBatch.setLayoutData( fdlBatch );

    wBatch = new Button( wMainComp, SWT.CHECK );
    props.setLook( wBatch );
    FormData fdBatch = new FormData();
    fdBatch.left = new FormAttachment( middle, 0 );
    fdBatch.top = new FormAttachment( wPartDaily, margin );
    fdBatch.right = new FormAttachment( 100, 0 );
    wBatch.setLayoutData( fdBatch );
    wBatch.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // Name in field
    wlNameInField = new Label( wMainComp, SWT.RIGHT );
    wlNameInField.setText( BaseMessages.getString( PKG, "TableOutputDialog.NameInField.Label" ) );
    props.setLook( wlNameInField );
    FormData fdlNameInField = new FormData();
    fdlNameInField.left = new FormAttachment( 0, 0 );
    fdlNameInField.right = new FormAttachment( middle, -margin );
    fdlNameInField.top = new FormAttachment( wBatch, margin );
    wlNameInField.setLayoutData( fdlNameInField );

    wNameInField = new Button( wMainComp, SWT.CHECK );
    props.setLook( wNameInField );
    FormData fdNameInField = new FormData();
    fdNameInField.left = new FormAttachment( middle, 0 );
    fdNameInField.top = new FormAttachment( wBatch, margin );
    fdNameInField.right = new FormAttachment( 100, 0 );
    wNameInField.setLayoutData( fdNameInField );
    wNameInField.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // Name field
    wlNameField = new Label( wMainComp, SWT.RIGHT );
    wlNameField.setText( BaseMessages.getString( PKG, "TableOutputDialog.NameField.Label" ) );
    props.setLook( wlNameField );
    FormData fdlNameField = new FormData();
    fdlNameField.left = new FormAttachment( 0, 0 );
    fdlNameField.right = new FormAttachment( middle, -margin );
    fdlNameField.top = new FormAttachment( wNameInField, margin );
    wlNameField.setLayoutData( fdlNameField );

    wNameField = new ComboVar( transMeta, wMainComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wNameField );
    wNameField.addModifyListener( lsMod );
    FormData fdNameField = new FormData();
    fdNameField.left = new FormAttachment( middle, 0 );
    fdNameField.top = new FormAttachment( wNameInField, margin );
    fdNameField.right = new FormAttachment( 100, 0 );
    wNameField.setLayoutData( fdNameField );
    wNameField.addFocusListener( new FocusAdapter() {
      @Override
      public void focusGained( FocusEvent e ) {
        getFields();
      }
    } );

    // Name in table
    wlNameInTable = new Label( wMainComp, SWT.RIGHT );
    wlNameInTable.setText( BaseMessages.getString( PKG, "TableOutputDialog.NameInTable.Label" ) );
    props.setLook( wlNameInTable );
    FormData fdlNameInTable = new FormData();
    fdlNameInTable.left = new FormAttachment( 0, 0 );
    fdlNameInTable.right = new FormAttachment( middle, -margin );
    fdlNameInTable.top = new FormAttachment( wNameField, margin );
    wlNameInTable.setLayoutData( fdlNameInTable );

    wNameInTable = new Button( wMainComp, SWT.CHECK );
    props.setLook( wNameInTable );
    FormData fdNameInTable = new FormData();
    fdNameInTable.left = new FormAttachment( middle, 0 );
    fdNameInTable.top = new FormAttachment( wNameField, margin );
    fdNameInTable.right = new FormAttachment( 100, 0 );
    wNameInTable.setLayoutData( fdNameInTable );
    wNameInTable.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Return keys
    wlReturnKeys = new Label( wMainComp, SWT.RIGHT );
    wlReturnKeys.setText( BaseMessages.getString( PKG, "TableOutputDialog.ReturnKeys.Label" ) );
    props.setLook( wlReturnKeys );
    FormData fdlReturnKeys = new FormData();
    fdlReturnKeys.left = new FormAttachment( 0, 0 );
    fdlReturnKeys.right = new FormAttachment( middle, -margin );
    fdlReturnKeys.top = new FormAttachment( wNameInTable, margin );
    wlReturnKeys.setLayoutData( fdlReturnKeys );

    wReturnKeys = new Button( wMainComp, SWT.CHECK );
    props.setLook( wReturnKeys );
    FormData fdReturnKeys = new FormData();
    fdReturnKeys.left = new FormAttachment( middle, 0 );
    fdReturnKeys.top = new FormAttachment( wNameInTable, margin );
    fdReturnKeys.right = new FormAttachment( 100, 0 );
    wReturnKeys.setLayoutData( fdReturnKeys );
    wReturnKeys.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // Return field
    wlReturnField = new Label( wMainComp, SWT.RIGHT );
    wlReturnField.setText( BaseMessages.getString( PKG, "TableOutputDialog.ReturnField.Label" ) );
    props.setLook( wlReturnField );
    FormData fdlReturnField = new FormData();
    fdlReturnField.left = new FormAttachment( 0, 0 );
    fdlReturnField.right = new FormAttachment( middle, -margin );
    fdlReturnField.top = new FormAttachment( wReturnKeys, margin );
    wlReturnField.setLayoutData( fdlReturnField );

    wReturnField = new TextVar( transMeta, wMainComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wReturnField );
    wReturnField.addModifyListener( lsMod );
    FormData fdReturnField = new FormData();
    fdReturnField.left = new FormAttachment( middle, 0 );
    fdReturnField.top = new FormAttachment( wReturnKeys, margin );
    fdReturnField.right = new FormAttachment( 100, 0 );
    wReturnField.setLayoutData( fdReturnField );

    wMainComp.layout();
    wMainTab.setControl( wMainComp );

    // ==== Fields Tab ====
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( MY_PKG, "TableOutputWithPreSQLDialog.FieldsTab.Title" ) );

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFieldsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = 3;
    fieldsLayout.marginHeight = 3;
    wFieldsComp.setLayout( fieldsLayout );

    // Get fields button
    wGetFields = new Button( wFieldsComp, SWT.PUSH );
    wGetFields.setText( BaseMessages.getString( PKG, "TableOutputDialog.GetFields.Button" ) );
    FormData fdGetFields = new FormData();
    fdGetFields.right = new FormAttachment( 50, -margin );
    fdGetFields.bottom = new FormAttachment( 100, 0 );
    wGetFields.setLayoutData( fdGetFields );

    // Do mapping button
    wDoMapping = new Button( wFieldsComp, SWT.PUSH );
    wDoMapping.setText( BaseMessages.getString( PKG, "TableOutputDialog.DoMapping.Button" ) );
    FormData fdDoMapping = new FormData();
    fdDoMapping.left = new FormAttachment( 50, margin );
    fdDoMapping.bottom = new FormAttachment( 100, 0 );
    wDoMapping.setLayoutData( fdDoMapping );

    int fieldsCols = 2;
    int fieldsRows = ( input.getFieldStream() != null ? input.getFieldStream().length : 1 );

    ciFields = new ColumnInfo[fieldsCols];
    ciFields[0] =
        new ColumnInfo(
            BaseMessages.getString( PKG, "TableOutputDialog.ColumnInfo.TableField" ),
            ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciFields[1] =
        new ColumnInfo(
            BaseMessages.getString( PKG, "TableOutputDialog.ColumnInfo.StreamField" ),
            ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    tableFieldColumns.add( ciFields[0] );

    wFields =
        new TableView( transMeta, wFieldsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
            | SWT.H_SCROLL, ciFields, fieldsRows, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wGetFields, -margin );
    wFields.setLayoutData( fdFields );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );

    // ==== Pre-SQL Tab ====
    wPreSQLTab = new CTabItem( wTabFolder, SWT.NONE );
    wPreSQLTab.setText( BaseMessages.getString( MY_PKG, "TableOutputWithPreSQLDialog.PreSQLTab.Label" ) );

    Composite wPreSQLComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wPreSQLComp );
    FormLayout preSQLLayout = new FormLayout();
    preSQLLayout.marginWidth = 3;
    preSQLLayout.marginHeight = 3;
    wPreSQLComp.setLayout( preSQLLayout );

    Label wlPreSQL = new Label( wPreSQLComp, SWT.NONE );
    wlPreSQL.setText( BaseMessages.getString( MY_PKG, "TableOutputWithPreSQLDialog.PreSQL.Label" ) );
    props.setLook( wlPreSQL );
    FormData fdlPreSQL = new FormData();
    fdlPreSQL.left = new FormAttachment( 0, 0 );
    fdlPreSQL.top = new FormAttachment( 0, 0 );
    wlPreSQL.setLayoutData( fdlPreSQL );

    wPreSQL =
        new StyledTextComp( transMeta, wPreSQLComp,
            SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "" );
    props.setLook( wPreSQL, Props.WIDGET_STYLE_FIXED );
    wPreSQL.addModifyListener( lsMod );
    FormData fdPreSQL = new FormData();
    fdPreSQL.left = new FormAttachment( 0, 0 );
    fdPreSQL.top = new FormAttachment( wlPreSQL, margin );
    fdPreSQL.right = new FormAttachment( 100, -2 * margin );
    fdPreSQL.bottom = new FormAttachment( 100, -margin );
    wPreSQL.setLayoutData( fdPreSQL );

    wPreSQLComp.layout();
    wPreSQLTab.setControl( wPreSQLComp );

    // ---- Tab Folder FormData ----
    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wSpecifyFields, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // ==== Buttons ====
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wOK.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( org.eclipse.swt.widgets.Event e ) {
        ok();
      }
    } );

    wCreate = new Button( shell, SWT.PUSH );
    wCreate.setText( BaseMessages.getString( PKG, "System.Button.SQL" ) );
    wCreate.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( org.eclipse.swt.widgets.Event e ) {
        sql();
      }
    } );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wCancel.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( org.eclipse.swt.widgets.Event e ) {
        cancel();
      }
    } );

    setButtonPositions( new Button[] { wOK, wCreate, wCancel }, margin, null );

    // Browse table button listener
    wbTable.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        getTableName();
      }
    } );

    // Browse schema button listener
    wbSchema.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        getSchemaNames();
      }
    } );

    // Get fields button listener
    wGetFields.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        get();
      }
    } );

    // Do mapping button listener
    wDoMapping.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        generateMappings();
      }
    } );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wStepname.addSelectionListener( lsDef );
    wCommit.addSelectionListener( lsDef );
    wSchema.addSelectionListener( lsDef );
    wTable.addSelectionListener( lsDef );
    wPartField.addSelectionListener( lsDef );
    wNameField.addSelectionListener( lsDef );
    wReturnField.addSelectionListener( lsDef );

    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

    // Load previous step fields in background
    new Thread() {
      @Override
      public void run() {
        try {
          StepMeta stepMeta = transMeta.findStep( stepname );
          if ( stepMeta != null ) {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );
            if ( row != null ) {
              for ( int i = 0; i < row.size(); i++ ) {
                inputFields.put( row.getValueMeta( i ).getName(), i );
              }
            }
            setComboBoxes();
          }
        } catch ( KettleException e ) {
          logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
        }
      }
    }.start();

    getData();
    setTableFieldCombo();
    setFlags();
    input.setChanged( changed );

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void getFields() {
    if ( !gotPreviousFields ) {
      gotPreviousFields = true;
      try {
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          String[] fieldNames = r.getFieldNames();
          String partField = wPartField.getText();
          String nameField = wNameField.getText();
          wPartField.setItems( fieldNames );
          wNameField.setItems( fieldNames );
          wPartField.setText( partField );
          wNameField.setText( nameField );
        }
      } catch ( KettleException ke ) {
        new ErrorDialog( shell,
            BaseMessages.getString( PKG, "TableOutputDialog.FailedToGetFields.DialogTitle" ),
            BaseMessages.getString( PKG, "TableOutputDialog.FailedToGetFields.DialogMessage" ), ke );
      }
    }
  }

  private void setTableFieldCombo() {
    Runnable fieldLoader = new Runnable() {
      @Override
      public void run() {
        if ( !wTable.isDisposed() && !wConnection.isDisposed() && !wSchema.isDisposed() ) {
          final String tableName = wTable.getText();
          final String connectionName = wConnection.getText();
          final String schemaName = wSchema.getText();
          if ( !Utils.isEmpty( tableName ) && !Utils.isEmpty( connectionName ) ) {
            DatabaseMeta ci = transMeta.findDatabase( connectionName );
            if ( ci != null ) {
              Database db = new Database( loggingObject, ci );
              try {
                db.connect();
                String schemaTable =
                    ci.getQuotedSchemaTableCombination( transMeta.environmentSubstitute( schemaName ),
                        transMeta.environmentSubstitute( tableName ) );
                RowMetaInterface r = db.getTableFields( schemaTable );
                if ( null != r ) {
                  String[] fieldNames = r.getFieldNames();
                  if ( null != fieldNames ) {
                    for ( ColumnInfo colInfo : tableFieldColumns ) {
                      colInfo.setComboValues( fieldNames );
                    }
                  }
                }
              } catch ( Exception e ) {
                for ( ColumnInfo colInfo : tableFieldColumns ) {
                  colInfo.setComboValues( new String[] {} );
                }
              } finally {
                db.disconnect();
              }
            }
          }
        }
      }
    };
    shell.getDisplay().asyncExec( fieldLoader );
  }

  private void setComboBoxes() {
    final Map<String, Integer> fields = new HashMap<>( inputFields );
    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<>( keySet );
    String[] fieldNames = entries.toArray( new String[0] );
    Const.sortStrings( fieldNames );
    ciFields[1].setComboValues( fieldNames );
  }

  private void setFlags() {
    // Partitioning and table-name-in-field are mutually exclusive
    boolean usePart = wUsePart.getSelection();
    boolean nameInField = wNameInField.getSelection();
    boolean specifyFields = wSpecifyFields.getSelection();
    boolean returnKeys = wReturnKeys.getSelection();

    wlPartField.setEnabled( usePart );
    wPartField.setEnabled( usePart );
    wlPartMonthly.setEnabled( usePart );
    wPartMonthly.setEnabled( usePart );
    wlPartDaily.setEnabled( usePart );
    wPartDaily.setEnabled( usePart );

    wlNameInField.setEnabled( !usePart );
    wNameInField.setEnabled( !usePart );
    wlNameField.setEnabled( nameInField && !usePart );
    wNameField.setEnabled( nameInField && !usePart );
    wlNameInTable.setEnabled( nameInField && !usePart );
    wNameInTable.setEnabled( nameInField && !usePart );

    wlUsePart.setEnabled( !nameInField );
    wUsePart.setEnabled( !nameInField );

    wlTruncate.setEnabled( !usePart && !nameInField );
    wTruncate.setEnabled( !usePart && !nameInField );

    wlReturnField.setEnabled( returnKeys );
    wReturnField.setEnabled( returnKeys );

    wFields.setEnabled( specifyFields );
    wGetFields.setEnabled( specifyFields );
    wDoMapping.setEnabled( specifyFields );
  }

  private void getData() {
    if ( input.getSchemaName() != null ) {
      wSchema.setText( input.getSchemaName() );
    }
    if ( input.getTableName() != null ) {
      wTable.setText( input.getTableName() );
    }
    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    }
    if ( input.getCommitSize() != null ) {
      wCommit.setText( input.getCommitSize() );
    }

    wTruncate.setSelection( input.truncateTable() );
    wIgnore.setSelection( input.ignoreErrors() );
    wSpecifyFields.setSelection( input.specifyFields() );
    wBatch.setSelection( input.useBatchUpdate() );

    wUsePart.setSelection( input.isPartitioningEnabled() );
    if ( input.getPartitioningField() != null ) {
      wPartField.setText( input.getPartitioningField() );
    }
    wPartMonthly.setSelection( input.isPartitioningMonthly() );
    wPartDaily.setSelection( input.isPartitioningDaily() );

    wNameInField.setSelection( input.isTableNameInField() );
    if ( input.getTableNameField() != null ) {
      wNameField.setText( input.getTableNameField() );
    }
    wNameInTable.setSelection( input.isTableNameInTable() );

    wReturnKeys.setSelection( input.isReturningGeneratedKeys() );
    if ( input.getGeneratedKeyField() != null ) {
      wReturnField.setText( input.getGeneratedKeyField() );
    }

    // Fields
    if ( input.getFieldStream() != null ) {
      for ( int i = 0; i < input.getFieldStream().length; i++ ) {
        TableItem item = wFields.table.getItem( i );
        if ( input.getFieldDatabase()[i] != null ) {
          item.setText( 1, input.getFieldDatabase()[i] );
        }
        if ( input.getFieldStream()[i] != null ) {
          item.setText( 2, input.getFieldStream()[i] );
        }
      }
    }

    // Pre-SQL
    if ( input.getPreSql() != null ) {
      wPreSQL.setText( input.getPreSql() );
    }

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void getInfo( TableOutputWithPreSQLMeta info ) {
    info.setSchemaName( wSchema.getText() );
    info.setTableName( wTable.getText() );
    info.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    info.setCommitSize( wCommit.getText() );
    info.setTruncateTable( wTruncate.getSelection() );
    info.setIgnoreErrors( wIgnore.getSelection() );
    info.setSpecifyFields( wSpecifyFields.getSelection() );
    info.setUseBatchUpdate( wBatch.getSelection() );

    info.setPartitioningEnabled( wUsePart.getSelection() );
    info.setPartitioningField( wPartField.getText() );
    info.setPartitioningDaily( wPartDaily.getSelection() );
    info.setPartitioningMonthly( wPartMonthly.getSelection() );

    info.setTableNameInField( wNameInField.getSelection() );
    info.setTableNameField( wNameField.getText() );
    info.setTableNameInTable( wNameInTable.getSelection() );

    info.setReturningGeneratedKeys( wReturnKeys.getSelection() );
    info.setGeneratedKeyField( wReturnField.getText() );

    int nrRows = wFields.nrNonEmpty();
    info.allocate( nrRows );
    for ( int i = 0; i < nrRows; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      info.getFieldDatabase()[i] = item.getText( 1 );
      info.getFieldStream()[i] = item.getText( 2 );
    }

    info.setPreSql( wPreSQL.getText() );
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }
    stepname = wStepname.getText();
    getInfo( input );

    if ( input.getDatabaseMeta() == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TableOutputDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "TableOutputDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }

    dispose();
  }

  private void getTableName() {
    DatabaseMeta inf = null;
    String connectionName = wConnection.getText();
    if ( !Utils.isEmpty( connectionName ) ) {
      inf = transMeta.findDatabase( connectionName );
    }
    if ( inf != null ) {
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "TableOutputDialog.Log.LookingAtConnection" ) + inf.toString() );
      }
      DatabaseExplorerDialog std = new DatabaseExplorerDialog( shell, SWT.NONE, inf, transMeta.getDatabases() );
      std.setSelectedSchemaAndTable( wSchema.getText(), wTable.getText() );
      if ( std.open() ) {
        wSchema.setText( Const.NVL( std.getSchemaName(), "" ) );
        wTable.setText( Const.NVL( std.getTableName(), "" ) );
        setTableFieldCombo();
      }
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TableOutputDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "TableOutputDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }
  }

  private void getSchemaNames() {
    DatabaseMeta databaseMeta = transMeta.findDatabase( wConnection.getText() );
    if ( databaseMeta != null ) {
      Database database = new Database( loggingObject, databaseMeta );
      try {
        database.connect();
        String[] schemas = database.getSchemas();
        if ( null != schemas && schemas.length > 0 ) {
          schemas = Const.sortStrings( schemas );
          EnterSelectionDialog dialog =
              new EnterSelectionDialog( shell, schemas,
                  BaseMessages.getString( PKG, "TableOutputDialog.AvailableSchemas.Title" ),
                  BaseMessages.getString( PKG, "TableOutputDialog.AvailableSchemas.Message" ) );
          String d = dialog.open();
          if ( d != null ) {
            wSchema.setText( Const.NVL( d, "" ) );
          }
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "TableOutputDialog.NoSchema.Error" ) );
          mb.setText( BaseMessages.getString( PKG, "TableOutputDialog.GetSchemas.Error" ) );
          mb.open();
        }
      } catch ( Exception e ) {
        new ErrorDialog( shell,
            BaseMessages.getString( PKG, "System.Dialog.Error.Title" ),
            BaseMessages.getString( PKG, "TableOutputDialog.ErrorGettingSchemas" ), e );
      } finally {
        database.disconnect();
      }
    }
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1, 2 }, new int[] {}, -1, -1, null );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell,
          BaseMessages.getString( PKG, "TableOutputDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "TableOutputDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  private void generateMappings() {
    // Determine source and target fields
    String[] sourceFields;
    String[] targetFields;
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      sourceFields = r.getFieldNames();
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
          BaseMessages.getString( PKG, "TableOutputDialog.DoMapping.UnableToFindSourceFields.Title" ),
          BaseMessages.getString( PKG, "TableOutputDialog.DoMapping.UnableToFindSourceFields.Message" ), e );
      return;
    }

    // Determine target fields
    String connectionName = wConnection.getText();
    if ( Utils.isEmpty( connectionName ) ) {
      return;
    }
    DatabaseMeta databaseMeta = transMeta.findDatabase( connectionName );
    if ( databaseMeta == null ) {
      return;
    }
    Database db = new Database( loggingObject, databaseMeta );
    try {
      db.connect();
      String schemaTable =
          databaseMeta.getQuotedSchemaTableCombination(
              transMeta.environmentSubstitute( wSchema.getText() ),
              transMeta.environmentSubstitute( wTable.getText() ) );
      RowMetaInterface targetRowMeta = db.getTableFields( schemaTable );
      targetFields = targetRowMeta.getFieldNames();
    } catch ( Exception e ) {
      new ErrorDialog( shell,
          BaseMessages.getString( PKG, "TableOutputDialog.DoMapping.UnableToFindTargetFields.Title" ),
          BaseMessages.getString( PKG, "TableOutputDialog.DoMapping.UnableToFindTargetFields.Message" ), e );
      return;
    } finally {
      db.disconnect();
    }

    // Build current mapping list
    List<SourceToTargetMapping> currentMappings = new ArrayList<>();
    int nrFields = wFields.nrNonEmpty();
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      String streamField = item.getText( 2 );
      String tableField = item.getText( 1 );
      int srcIdx = -1;
      int tgtIdx = -1;
      for ( int s = 0; s < sourceFields.length; s++ ) {
        if ( sourceFields[s].equals( streamField ) ) {
          srcIdx = s;
          break;
        }
      }
      for ( int t = 0; t < targetFields.length; t++ ) {
        if ( targetFields[t].equals( tableField ) ) {
          tgtIdx = t;
          break;
        }
      }
      if ( srcIdx >= 0 && tgtIdx >= 0 ) {
        currentMappings.add( new SourceToTargetMapping( srcIdx, tgtIdx ) );
      }
    }

    EnterMappingDialog d =
        new EnterMappingDialog( shell, sourceFields, targetFields, currentMappings );
    List<SourceToTargetMapping> mappings = d.open();
    if ( mappings != null ) {
      wFields.table.removeAll();
      wFields.table.setItemCount( mappings.size() );
      for ( int i = 0; i < mappings.size(); i++ ) {
        SourceToTargetMapping mapping = mappings.get( i );
        TableItem item = wFields.table.getItem( i );
        item.setText( 2, sourceFields[mapping.getSourcePosition()] );
        item.setText( 1, targetFields[mapping.getTargetPosition()] );
      }
      wFields.setRowNums();
      wFields.optWidth( true );
    }
  }

  private void sql() {
    try {
      TableOutputWithPreSQLMeta info = new TableOutputWithPreSQLMeta();
      getInfo( info );
      RowMetaInterface prev = transMeta.getPrevStepFields( stepname );
      StepMeta stepMeta = transMeta.findStep( stepname );

      if ( info.specifyFields() ) {
        int nrFields = wFields.nrNonEmpty();
        RowMetaInterface prevNew = prev.clone();
        prevNew.clear();
        for ( int i = 0; i < nrFields; i++ ) {
          TableItem item = wFields.getNonEmpty( i );
          String streamField = item.getText( 2 );
          int idx = prev.indexOfValue( streamField );
          if ( idx >= 0 ) {
            ValueMetaInterface v = prev.getValueMeta( idx ).clone();
            String tableField = item.getText( 1 );
            v.setName( tableField );
            prevNew.addValueMeta( v );
          }
        }
        prev = prevNew;
      }

      boolean autoInc = false;
      String pk = null;
      if ( info.isReturningGeneratedKeys() && !Utils.isEmpty( info.getGeneratedKeyField() ) ) {
        autoInc = true;
        pk = info.getGeneratedKeyField();
      }

      SQLStatement sql =
          info.getSQLStatements( transMeta, stepMeta, prev, pk, autoInc, null );
      if ( !sql.hasError() ) {
        if ( sql.hasSQL() ) {
          SQLEditor sqledit =
              new SQLEditor( transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(),
                  sql.getSQL() );
          sqledit.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
          mb.setMessage( BaseMessages.getString( PKG, "TableOutputDialog.NoSQLNeeds.DialogMessage" ) );
          mb.setText( BaseMessages.getString( PKG, "TableOutputDialog.NoSQLNeeds.DialogTitle" ) );
          mb.open();
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( sql.getError() );
        mb.setText( BaseMessages.getString( PKG, "TableOutputDialog.SQLError.DialogTitle" ) );
        mb.open();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell,
          BaseMessages.getString( PKG, "TableOutputDialog.BuildSQLError.DialogTitle" ),
          BaseMessages.getString( PKG, "TableOutputDialog.BuildSQLError.DialogMessage" ), ke );
    }
  }
}
