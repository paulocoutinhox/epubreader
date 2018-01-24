#include <chrono>
#include <thread>
#include <EpubReaderServerImpl.hpp>

#define DOCUMENT_ROOT "../www"
#define EPUB_FILE "../data/ebook.epub"

bool canExit = false;

void create() 
{
	auto server =  EpubReader::EpubReaderServer::create(DOCUMENT_ROOT, EPUB_FILE);
	server->start();
}

int main(int argc, char *argv[])
{
	// test of memory dealloc - only for reference - let commented
	// create();

	auto server = EpubReader::EpubReaderServer::create(DOCUMENT_ROOT, EPUB_FILE);
	server->start();

	while (!canExit)
	{
		std::this_thread::sleep_for(std::chrono::milliseconds(1));
	}

	printf("Bye!\n");

	return 0;
}
