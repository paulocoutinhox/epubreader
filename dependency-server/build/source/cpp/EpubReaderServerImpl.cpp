#include "EpubReaderServerImpl.hpp"
#include <iostream>
#include <string>
#include <sstream>

namespace EpubReader {

std::shared_ptr<EpubReaderServer> EpubReaderServer::create(const std::string & document_root, const std::string & epub_file)
{
    return std::make_shared<EpubReaderServerImpl>(document_root, epub_file);
}

EpubReaderServerImpl::EpubReaderServerImpl(const std::string & documentRoot, const std::string & epubFile)
{   
    std::cout << "EpubReaderServerImpl::EpubReaderServerImpl" << std::endl; 

    this->documentRoot = documentRoot;
    this->epubFile = epubFile;
    this->port = 19090;
}

EpubReaderServerImpl::~EpubReaderServerImpl()
{    
    std::cout << "EpubReaderServerImpl::~EpubReaderServerImpl" << std::endl;
}

void EpubReaderServerImpl::start()
{
    std::ostringstream portStr;
    portStr << port;
    
    const char *options[] = {
		"document_root",
		documentRoot.c_str(),
		"listening_ports",
		portStr.str().c_str(),
		0
	};

    std::vector<std::string> cppOptions;

	for (int i = 0; i < (sizeof(options) / sizeof(options[0]) - 1); i++)
	{
		cppOptions.push_back(options[i]);
	}

    if (server != nullptr) {
        server->close();
    }
    
    epubHandler = std::make_shared<EpubHandler>();
    epubHandler->setEpubFile(epubFile);

    server = std::make_shared<CivetServer>(cppOptions);    
    server->addHandler("/ebook.epub", &(*epubHandler));

    printf("Server started at http://localhost:%d\n", port);
	printf("Get epub at http://localhost:%d/ebook.epub\n", port);
}

void EpubReaderServerImpl::stop()
{
    if (server != nullptr) {
        server->close();        
    }

    server = nullptr;
}
    
}