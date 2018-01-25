#include <EpubReaderServer.hpp>
#include <string>
#include <memory>
#include <EpubHandler.hpp>
#include <CivetServer.h>

namespace EpubReader {

class EpubReaderServerImpl: public EpubReaderServer {
    
public:
    EpubReaderServerImpl(const std::string & documentRoot, const std::string & epubFile);
    ~EpubReaderServerImpl();

    void start();
    void stop();

private:
    std::string documentRoot;
    std::string epubFile;
    int port;
    
    std::shared_ptr<EpubHandler> epubHandler;
    std::shared_ptr<CivetServer> server;

};

}