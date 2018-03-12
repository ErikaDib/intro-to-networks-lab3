
import java.io.*;
import java.net.*;
import java.util.*;

final class ProxyRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;

	// Constructor
	public ProxyRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	// Implement the run() method of the Runnable interface.
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void processRequest() throws Exception {
		// Get a reference to the socket's input and output streams.
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		// Set up input stream filters.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Get the request line of the HTTP request message.
		String requestLine = br.readLine();

		// Extract the filename from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which should be "GET"
		String fileName = tokens.nextToken();

		String urlName = fileName; // use for downloading html page

		// check to see if input on requestline is a directory
		String dir = System.getProperty("user.dir") + fileName; // get directory
																// path
		File f = new File(dir);
		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;
		String url = "http:/" + urlName;
		if (isValid(url) && DownloadWebPage(url)) {
			fileName = "./Proxy.html";
			requestLine = "/Proxy.html";
		}

		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}

		// Debug info for private use
		System.out.println("Incoming!!!");
		System.out.println(requestLine);
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}

		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;

		if (fileExists) {
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-Type: " + contentType(fileName) + CRLF;

		}

		else if (f.isDirectory()) { // check if input is a directory , no input
									// will default to current directory
			System.out.println("is a directory!");
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-Type: text/html" + CRLF;

			// File array to store all files within the directory
			File[] fileList = new File(dir).listFiles();
			System.out.println("current dir: " + dir);

			// using stringbuilder so when its return as tostring, it won't show
			// brackets or commas
			StringBuilder dirFiles = new StringBuilder();
			String fname = "";
			String hyperlink = "";
			for (File file : fileList) {
				if (file.isFile()) {
					fname = file.getName();
					hyperlink = "<li><a target='_blank' href='./" + fname + "'>" + fname + "</a></li>";
					dirFiles.append(hyperlink);
				} else if (file.isDirectory()) {
					fname = file.getName();
					hyperlink = "<li><a href='./" + fname + "/'>" + fname + "/</li>";
					dirFiles.append(hyperlink);
				}
			}

			String formattedFiles = dirFiles.toString(); // convert string array
															// to a string
			entityBody = "<HTML>" + "<HEAD><TITLE>Directory: </TITLE></HEAD>" + "<BODY><b>Directory</br>" + dir
					+ "</b></br><I>Files</I></br><li><a href='../'>..</a></li>" + formattedFiles + "</BODY></HTML>";
		} else {
			statusLine = "HTTP/1.0 404 Not Found" + CRLF;
			contentTypeLine = "Content-Type: text/html" + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>not found " + fileName
					+ "</br>request: " + requestLine + "</BODY></HTML>";
		}
		// Send the status line.
		os.writeBytes(statusLine);

		// Send the content type line.
		os.writeBytes(contentTypeLine);

		// Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);

		// Send the entity body.
		if (fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}

		// Close streams and socket.
		os.close();
		br.close();
		socket.close();
	}

	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;

		// Copy requested file into the socket's output stream.
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}

	private static String contentType(String fileName) {

		// if file is an image, return an image
		if (fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".png")
				|| fileName.endsWith(".gif")) {

			return "image"; // html/image will download the file instead

		} else {
			// file will be return and viewed in text
			return "text/html";

		}

		/*
		 * if(fileName.endsWith(".htm") || fileName.endsWith(".html")) { return
		 * "text/html"; } if(fileName.endsWith(".ram") ||
		 * fileName.endsWith(".ra")) { return "audio/x-pn-realaudio"; } return
		 * "application/octet-stream" ;
		 */
	}

	private static boolean DownloadWebPage(String webpage) {
		try {

			// Create URL object
			URL url = new URL(webpage);
			BufferedReader readr = new BufferedReader(new InputStreamReader(url.openStream()));

			// Enter filename in which you want to download
			BufferedWriter writer = new BufferedWriter(new FileWriter("Proxy.html"));

			// read each line from stream till end
			String line;
			while ((line = readr.readLine()) != null) {
				writer.write(line);
			}

			readr.close();
			writer.close();
			System.out.println("Successfully Downloaded!");
			return true;

		}

		// Exceptions
		catch (MalformedURLException mue) {
			System.out.println("Malformed URL Exception raised");
			return false;
		} catch (IOException ie) {
			System.out.println("IOException raised");
			return false;
		}

	}

	private static boolean isValid(String url) {
		/* Try to create a valid URL */
		try {
			new URL(url).toURI();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

}
