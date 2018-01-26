#include "EpubReaderServerImpl.hpp"
#include <iostream>
#include <string>
#include <sstream>

namespace EpubReader {

std::shared_ptr<EpubReaderServer> EpubReaderServer::create(const std::string & port, const std::string & document_root, const std::string & epub_file)
{
    return std::make_shared<EpubReaderServerImpl>(port, document_root, epub_file);
}

EpubReaderServerImpl::EpubReaderServerImpl(const std::string & port, const std::string & documentRoot, const std::string & epubFile)
{   
    std::cout << "EpubReaderServerImpl::EpubReaderServerImpl" << std::endl; 

    this->port = port;
    this->documentRoot = documentRoot;
    this->epubFile = epubFile;    
}

EpubReaderServerImpl::~EpubReaderServerImpl()
{    
    std::cout << "EpubReaderServerImpl::~EpubReaderServerImpl" << std::endl;
}

void EpubReaderServerImpl::start()
{
    std::string errorLogFilePath = documentRoot + "/epubreader.log";
    
    const char *options[] = {
		"document_root",
		documentRoot.c_str(),
		"listening_ports",
		port.c_str(),
        "error_log_file",
        errorLogFilePath.c_str(),
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

    printf("Server started at http://localhost:%s\n", port.c_str());
	printf("Get epub at http://localhost:%s/ebook.epub\n", port.c_str());
}

void EpubReaderServerImpl::stop()
{
    if (server != nullptr) {
        server->close();        
    }

    server = nullptr;
}
    
}