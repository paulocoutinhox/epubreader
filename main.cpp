#include "CivetServer.h"
#include <cstring>
#include <unistd.h>
#include <fstream>
#include <sstream>

#define DOCUMENT_ROOT "../www"
#define PORT "9090"
#define EPUB_FILE "../data/ebook.epub"

bool exitNow = false;

class EPubHandler : public CivetHandler
{
public:
	bool handleGet(CivetServer *server, struct mg_connection *conn)
	{
		mg_send_mime_file(conn, EPUB_FILE, "application/epub+zip");
		return true;
	}
};

int main(int argc, char *argv[])
{
	const char *options[] = {
		"document_root",
		DOCUMENT_ROOT,
		"listening_ports",
		PORT,
		0
	};

	std::vector<std::string> cpp_options;

	for (int i = 0; i < (sizeof(options) / sizeof(options[0]) - 1); i++)
	{
		cpp_options.push_back(options[i]);
	}

	CivetServer server(cpp_options);

	EPubHandler h_epub;
	server.addHandler("/ebook.epub", h_epub);
	printf("Get epub at http://localhost:%s/ebook.epub\n", PORT);

	while (!exitNow)
	{
		sleep(1);
	}

	printf("Bye!\n");

	return 0;
}
