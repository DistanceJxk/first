package com.itheima.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class LucnenDemo {
	
	
	/**
	 * 创建索引
	 * @throws IOException
	 */
	@Test
	public void testAddIndex() throws IOException {
		//1.执行索引库目录
		Directory directory = FSDirectory.open(new File("D:\\文件夹\\sucene"));
		//2.指定分词器
		IKAnalyzer analyzer = new IKAnalyzer();
		//3.创建一个配置对象
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,analyzer);
		//4.创建一个 写入索引对象
		IndexWriter indexWriter = new IndexWriter(directory,config);
		//5.写入对象了
		File files = new File("D:\\文件夹\\上课用的查询资料searchsource");
		File[] listFiles = files.listFiles();
		for (File file : listFiles) {
			Document doc = new Document();
			//文件名称
			Field fileNameField = new TextField("name", file.getName(), Store.YES);
			doc.add(fileNameField);
			//文件路径
			Field filePathField = new TextField("path", file.getPath(),Store.YES);
			doc.add(filePathField);
			//文件大小  单位b
			long sizeOf = FileUtils.sizeOf(file);
			Field fileSizeField = new TextField("size", sizeOf+"",Store.YES);
			doc.add(fileSizeField);
			//文件内容
			String fileContent = FileUtils.readFileToString(file);
			Field fileContentField = new TextField("content", fileContent,Store.YES);
			doc.add(fileContentField);
			indexWriter.addDocument(doc);
 		}
		//关闭IndexWriter对象
		indexWriter.close();
	}
	/**
	 * 查询索引
	 * @throws Exception 
	 */
	
	@Test
	public void testSearchIndex() throws Exception {
		//指定索引库的目录
		Directory directory = FSDirectory.open(new File("D:\\文件夹\\sucene"));
		//创建一个读取索引对象
		DirectoryReader indexReader = DirectoryReader.open(directory);
		//创建一个搜索索引的对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//执行查询
		//term查询
		//Query query = new TermQuery(new Term("name","new"));
		//查询全部
		//Query query = new  MatchAllDocsQuery();
		//区间查询
		//Query query = NumericRangeQuery.newLongRange("size", 10l, 100l, true, true);
		
//		BooleanQuery query = new BooleanQuery();
//		Query query1 = new TermQuery(new Term("name","spring"));
//		Query query2 = new TermQuery(new Term("content","spring"));
//		query.add(query1,Occur.MUST);
//		query.add(query2,Occur.SHOULD);
		//分词查询
		QueryParser qp = new MultiFieldQueryParser(new String [] {"name","content"}, new IKAnalyzer());
		Query query = qp.parse("spring is project");
		System.out.println("查询语法："+query);
		TopDocs topDocs = indexSearcher.search(query, 100);
		System.out.println("总条数"+topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			int docId = scoreDoc.doc;
			Document doc = indexSearcher.doc(docId);
			System.out.println(doc.get("name"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("path"));
			System.out.println(doc.get("size"));
			System.out.println("--------------------");
		}
		//关闭资源
		indexReader.close();
	}
	
	/**
	 * 删除索引
	 * @throws Exception
	 */
	@Test
	public void testDeleteIndex() throws Exception {
		Directory directory = FSDirectory.open(new File("D:\\文件夹\\sucene"));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,analyzer);
		IndexWriter indexWriter = new IndexWriter(directory,config);
		indexWriter.deleteAll();
		indexWriter.close();
		
	}
	
	/**
	 * 更新索引
	 * @throws IOException 
	 */

	@Test
	public void testUpdateIndex() throws IOException {
		Directory directory = FSDirectory.open(new File("D:\\文件夹\\sucene"));
		IKAnalyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,analyzer);
		IndexWriter indexWriter = new IndexWriter(directory,config);
		Document doc = new Document();
		doc.add(new TextField("name", "new",Store.YES));
		doc.add(new StoredField("path", ""));
		doc.add(new LongField("size", 100l, Store.YES));
		doc.add(new StringField("content", "更新后的内容spring", Store.YES));
		
		indexWriter.updateDocument(new Term("name", "spring"), doc);
		indexWriter.close();
	}
}
