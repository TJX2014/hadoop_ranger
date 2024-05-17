package org.apache.hive;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.SQLConfHelper;
import org.apache.spark.sql.catalyst.TableIdentifier;
import org.apache.spark.sql.catalyst.analysis.*;
import org.apache.spark.sql.catalyst.catalog.CatalogStorageFormat;
import org.apache.spark.sql.catalyst.catalog.CatalogTable;
import org.apache.spark.sql.catalyst.catalog.CatalogTableType;
import org.apache.spark.sql.connector.catalog.*;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.execution.datasources.text.TextFileFormat;
import org.apache.spark.sql.execution.datasources.v2.text.TextTable;
import org.apache.spark.sql.hive.HiveExternalCatalog;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import scala.Option;
import scala.collection.Seq;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.ArraySeq;

import java.util.*;
import java.util.stream.Collectors;

public class HiveCatalog implements SupportsNamespaces, TableCatalog, SQLConfHelper {

  private String catalogName = null;

  private HiveExternalCatalog catalog = null;
  public static void main(String[] args) {
//    SparkSession.active().conf();
//    HiveExternalCatalog catalog = new HiveExternalCatalog(new SparkConf(), new Configuration());
//    System.out.println(catalog);

    List<String> res = Arrays.asList("aaa");
    Seq<String> result = scala.collection.JavaConverters.asScalaIterator(res.iterator()).toSeq();
    System.out.println(result);

    List<String> inputs = new ArrayList<>(2);
    inputs.add("aaa1");
    inputs.add("bb1");

    String[][] array = new HiveCatalog().toMultiArray(inputs);

    System.out.println(array);
  }

  private String[][] toMultiArray(List<String> inputs) {
    int size = inputs.size();
    String[][] array = new String[size][];
    for (int i=0; i < size; i++) {
      List<String> input = new ArrayList<>(1);
      input.add(inputs.get(i));
      String[] inputArr = input.toArray(new String[0]);
      array[i] = inputArr;
    }
    return array;
  }

  @Override
  public void initialize(String s, CaseInsensitiveStringMap caseInsensitiveStringMap) {
    this.catalogName = s;
    Configuration hadoopConf = new Configuration();
    caseInsensitiveStringMap.forEach((k, v) -> hadoopConf.set(k, v));
    this.catalog = new HiveExternalCatalog(new SparkConf(), hadoopConf);
  }

  @Override
  public String name() {
    return catalogName;
  }

  @Override
  public Identifier[] listTables(String[] strings) throws NoSuchNamespaceException {
    Seq<String> tableSeq;
    if (strings.length > 0) {
      String ns = strings[0];
      tableSeq = this.catalog.listTables(ns);
    } else {
      tableSeq = this.catalog.listTables("*");
    }

    Collection<String> tables = scala.collection.JavaConversions.asJavaCollection(tableSeq);
    return tables.stream().map(t -> Identifier.of(strings, t)).collect(Collectors.toList()).toArray(new Identifier[0]);
  }

  @Override
  public Table loadTable(Identifier identifier) throws NoSuchTableException {
    String db = identifier.namespace()[0];
    String table = identifier.name();
    CatalogTable catalogTable = this.catalog.getTable(db, table);
    SparkSession session = SparkSession.getActiveSession().getOrElse(null);
    CaseInsensitiveStringMap options = new CaseInsensitiveStringMap(
        scala.collection.JavaConversions.mapAsJavaMap(catalogTable.properties())
    );

    List<String> res = Arrays.asList(catalogTable.location().toString());
    Seq<String> paths = scala.collection.JavaConverters.asScalaIterator(res.iterator()).toSeq();

    return new TextTable(catalogTable.qualifiedName(),
            session, options, paths, Option.apply(catalogTable.schema()), TextFileFormat.class);
  }

  @Override
  public Table createTable(Identifier identifier, StructType schema, Transform[] transforms, Map<String, String> properties) throws TableAlreadyExistsException, NoSuchNamespaceException {
    boolean isExternal = properties.containsKey(TableCatalog.PROP_EXTERNAL);
    Optional<String> location = Optional.ofNullable(properties.get(TableCatalog.PROP_LOCATION));
    CatalogTableType tableType;
    if (isExternal || location.isPresent()) {
      tableType = CatalogTableType.EXTERNAL();
    } else {
      tableType = CatalogTableType.MANAGED();
    }

    CatalogStorageFormat storageFormat = new CatalogStorageFormat(
        Option.empty(), Option.apply("org.apache.hadoop.mapred.TextInputFormat"),
        Option.apply("org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat"),
        Option.apply("org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"),
        false, new HashMap<>()
    );
    String db = identifier.namespace()[0];
    String table = identifier.name();

    String owner = "";
    long createTime = System.currentTimeMillis();
    HashMap props = new HashMap<String, String>();
    String provider = properties.getOrDefault(TableCatalog.PROP_PROVIDER, conf().defaultDataSourceName());
    TableIdentifier tableIdentifier = new TableIdentifier(table, Option.apply(db));
    CatalogTable catalogTable = new CatalogTable(
        tableIdentifier, tableType, storageFormat, schema,
        Option.apply(provider), new ArrayBuffer<>(), Option.empty(),
        owner, createTime, createTime, "3.1.2", props,
        Option.empty(), Option.empty(), Option.empty(), new ArraySeq<>(0),
        false, false, new HashMap<>(), Option.empty());
    this.catalog.createTable(catalogTable, false);
    try {
      return loadTable(identifier);
    } catch (NoSuchTableException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Table alterTable(Identifier identifier, TableChange... tableChanges) throws NoSuchTableException {
    return null;
  }

  @Override
  public boolean dropTable(Identifier identifier) {
    String db = identifier.namespace()[0];
    String table = identifier.name();
    catalog.dropTable(db, table, true, true);
    return true;
  }

  @Override
  public void renameTable(Identifier identifier, Identifier identifier1) throws NoSuchTableException, TableAlreadyExistsException {
    System.out.println("" + identifier + identifier1);
  }

  @Override
  public SQLConf conf() {
    return SQLConfHelper.super.conf();
  }

  @Override
  public String[][] listNamespaces() throws NoSuchNamespaceException {
    List<String> namespaces =
        scala.collection.JavaConversions.seqAsJavaList(this.catalog.listDatabases());
    return toMultiArray(namespaces);
  }

  @Override
  public String[][] listNamespaces(String[] strings) throws NoSuchNamespaceException {
    return new String[0][];
  }

  @Override
  public Map<String, String> loadNamespaceMetadata(String[] strings) throws NoSuchNamespaceException {
    return null;
  }

  @Override
  public void createNamespace(String[] strings, Map<String, String> map) throws NamespaceAlreadyExistsException {

  }

  @Override
  public void alterNamespace(String[] strings, NamespaceChange... namespaceChanges) throws NoSuchNamespaceException {

  }

  @Override
  public boolean dropNamespace(String[] strings, boolean b) throws NoSuchNamespaceException, NonEmptyNamespaceException {
    return false;
  }
}
