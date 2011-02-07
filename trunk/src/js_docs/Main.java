package js_docs;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import turkitBridge.TurKit;
import util.MyStringWriter;
import util.U;

public class Main {
	public static int funcCount = 0;
	
	public static void process(ArrayList<URL> urls, MyStringWriter funcs,
			MyStringWriter funcIndex) throws Exception {
		MyStringWriter out = new MyStringWriter();
		for (URL url : urls) {
			out.print(U.slurp(url));
		}
		String s = out.toString();

		Matcher m = Pattern.compile("(?msi)/\\*\\*(.*?)\\*/\\s*(.*?)\r?\n")
				.matcher(s);
		while (m.find()) {
			String func = m.group(2);
			func = func.replaceAll("=|\\s+|function|\\{|\\}", "").replaceAll(
					",", ", ");
			String sym = func.replaceAll("\\(.*", "");
			String funcHtml = func.replaceAll("(\\(|,)(\\s*\\w+)",
					"$1<span class=\"param\">$2</span>").replaceAll(
					"^((\\w+\\.)+)", "<span class=\"prefix\">$1</span>");
			funcs.println("<a name=\"" + sym + "\"><div class=\"func\">"
					+ funcHtml + "</div></a>");

			String doc = m.group(1);
			doc = doc.replaceAll("\n[ \t]+(\\*[ \t]*)?", "\n");
			doc = doc.replaceAll("\\{@link (.*?)\\}", "<a href=\"#$1\">$1</a>");
			funcs.println("<div class=\"doc\">" + doc + "</div>");

			String prefix = sym.replaceAll("\\w+$", "");
			String name = U.match(sym, "(\\w+)$", 1);
			if (prefix.length() > 0) {
				funcIndex
						.println("<div class=\"index-func indent\"><a href=\"#"
								+ sym + "\">" + name + "</a></div>");
			} else {
				funcIndex.println("<div class=\"index-func\"><a href=\"#" + sym
						+ "\">" + name + "</a></div>");
			}
			funcCount++;
		}
	}

	public static void main(String[] args) throws Exception {
		MyStringWriter funcs = new MyStringWriter();
		ArrayList<MyStringWriter> funcIndexes = new ArrayList();

		ArrayList<URL> urls = new ArrayList();
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/CrashAndRerun.js"));
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/Database.js"));
		MyStringWriter funcIndex = new MyStringWriter();
		funcIndexes.add(funcIndex);
		process(urls, funcs, funcIndex);

		urls.clear();
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/MTurk.js"));
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/MTurkBase.js"));
		funcIndex = new MyStringWriter();
		funcIndexes.add(funcIndex);
		process(urls, funcs, funcIndex);

		urls.clear();
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/Webpage.js"));
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/WebpageBase.js"));
		funcIndex = new MyStringWriter();
		funcIndexes.add(funcIndex);
		process(urls, funcs, funcIndex);

		urls.clear();
		urls.add(Main.class.getResource(TurKit.js_libs_path + "/util.js"));
		funcIndex = new MyStringWriter();
		funcIndexes.add(funcIndex);
		process(urls, funcs, funcIndex);

		String s = U.slurp(Main.class.getResource("index.html")).replace(
				"___FUNCS___", funcs.toString());

		funcIndex = new MyStringWriter();
		for (MyStringWriter fi : funcIndexes) {
			funcIndex.print("<div class=\"index-list\">" + fi.toString()
					+ "</div>");
		}
		s = s.replace("___FUNC_INDEX___", funcIndex.toString());

		File dir = new File("war/jsdocs");
		dir.mkdirs();
		PrintWriter html = new PrintWriter(new File(dir.getCanonicalPath()
				+ "/index.html"));
		html.print(s);
		html.close();

		System.out.println("done: " + funcCount + " funcs");
	}
}
