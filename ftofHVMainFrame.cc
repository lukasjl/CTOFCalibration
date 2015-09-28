#include "ftofHVMainFrame.h"
#include <iostream>
#include <fstream>
#include "FtofHistogramManager.h"
#include "TStyle.h"
#include "FtofRawEvio.h"
#include "FtofGeometricMean.h"
#include "FtofLogRatio.h"
#include "TGFileDialog.h"
#include <cmath>
#include "TPaveLabel.h"
#include "ftofHVAboutDialog.h"
#include "ftofHVConfigPar.h"
#include "FtofCalibration.h"
#include "ftofRawEvioRoot.h"
using namespace std;
using namespace ftof;
void FtofHVMainFrame::DoNew(){
  mStatus=1;
  UpdateStatusBar();
  TGFileInfo fileInfo;
  const char *filetypes[] = {"All files", "*.*", 0, 0};
  fileInfo.fFileTypes = filetypes;
  fileInfo.fIniDir = StrDup(".");
  new TGFileDialog(gClient->GetRoot(), gClient->GetRoot(), kFDOpen, &fileInfo);  
  if(fileInfo.fFilename!=NULL){//determine if it's a list or a root file
    mNameFilelist=fileInfo.fFilename;
    UpdateStatusBar();
    int lenName=strlen(fileInfo.fFilename);
    mIsFilelist=true;
    if(lenName>5 && fileInfo.fFilename[lenName-5]=='.' && toupper(fileInfo.fFilename[lenName-4])=='R' && toupper(fileInfo.fFilename[lenName-3])=='O'&& toupper(fileInfo.fFilename[lenName-2])=='O'&& toupper(fileInfo.fFilename[lenName-1])=='T')
      mIsFilelist=false;
    DoCalibration();
  }

}
void FtofHVMainFrame::DoAbout(){
  new FtofHVAboutDialog(gClient->GetRoot(),this,400,300);

}
void FtofHVMainFrame::ReadHV(int panel){
  mStatus=10+panel;
  UpdateStatusBar();
  TGFileInfo fileInfo;
  const char *filetypes[] = {"text file", "*.txt", 0, 0};
  fileInfo.fFileTypes = filetypes;
  fileInfo.fIniDir = StrDup(".");
  new TGFileDialog(gClient->GetRoot(), gClient->GetRoot(), kFDOpen, &fileInfo);  
  if(fileInfo.fFilename!=NULL){
    ifstream fileHV(fileInfo.fFilename);
    while(fileHV.good()){
      double paddle,left,right;
      fileHV>>paddle>>left>>right;
      if(fileHV.good()){
	UInt_t mySector=(int(paddle*100+0.1))/100;
	UInt_t myPaddle=(int(paddle*100+0.1))%100;
	switch (panel){
	case 0:
	  break;
	case 1:
	  myPaddle+=mNPanel1A;
	  break;
	case 2:
	  myPaddle+=mNPanel1A+mNPanel1B;
	}
	if(myPaddle>mNPanel1A+mNPanel1B+mNPanel2) break;
	mHVOld[mySector-1][myPaddle-1][0]=left;
	mHVOld[mySector-1][myPaddle-1][1]=right;
      }
    }

  }
  mStatus=0;
  UpdateStatusBar();
}

void FtofHVMainFrame::DoLoad(){
  mStatus=3;
  UpdateStatusBar();
  TGFileInfo fileInfo;
  const char *filetypes[] = {"Root file", "*.root", 0, 0};
  fileInfo.fFileTypes = filetypes;
  fileInfo.fIniDir = StrDup(".");
  new TGFileDialog(gClient->GetRoot(), gClient->GetRoot(), kFDOpen, &fileInfo);  
  if(fileInfo.fFilename!=NULL)
    ImportRoot(fileInfo.fFilename);
  ViewStat();
}
void FtofHVMainFrame::ImportRoot(char* nameFile){
  FtofHistogramManager hm;
  string name;
  TH1D* h1;
  TFile* fileIn=new TFile(nameFile);
  for(int iSector=0;iSector<6;iSector++){
    name="stat_sector";
    name+=iSector+1+'0';
    h1=(TH1D*)fileIn->Get(name.c_str());
    if(h1!=NULL) hm.addH1(h1);

    name="guiHV_New_HV_Left_";
    name+=iSector+1+'0';
    h1=(TH1D*)fileIn->Get(name.c_str());
    if(h1!=NULL) hm.addH1(h1);

    name="guiHV_New_HV_Right_";
    name+=iSector+1+'0';
    h1=(TH1D*)fileIn->Get(name.c_str());
    if(h1!=NULL) hm.addH1(h1);

    for(UInt_t iPaddle=0;iPaddle<90;iPaddle++){
      name="GeoMean_";
      name+=iSector+1+'0';
      name+='0';
      name+=iPaddle/10+'0';
      name+=iPaddle%10+'0';
      h1=(TH1D*)fileIn->Get(name.c_str());
      if(h1!=NULL){
	hm.addH1(h1);
      }
      name="LogRatio_";
      name+=iSector+1+'0';
      name+='0';
      name+=iPaddle/10+'0';
      name+=iPaddle%10+'0';
      h1=(TH1D*)fileIn->Get(name.c_str());
      if(h1!=NULL){
	hm.addH1(h1);
      }
    }

  }
  
}
void FtofHVMainFrame::calculateNewHV(double gain_in,double centroid,double& newLeft,double& newRight,double& errorLeft,double& errorRight,int option){
  /********************************88
   * option=0:1A, 1:1B, 2:2
   */
  const double Alpha_1a=13.6;
  const double Alpha_1b=4.7;
  const double Alpha_2=8.6;
  const double Mip_1a=800;
  const double Mip_1b=2000;
  const double Mip_2=800;

  double alpha,gain;
  switch(option){
  case 0:
    alpha=Alpha_1a;
    gain=Mip_1a;
    break;
  case 1:
    alpha=Alpha_1b;
    gain=Mip_1b;
    break;
  case 2:
    alpha=Alpha_2;
    gain=Mip_2;
    break;
  }

  double gainLeft=gain_in/sqrt(exp(centroid));
  double gainRight=gain_in*sqrt(exp(centroid));
  double deltaGainLeft=gain-gainLeft;
  double deltaGainRight=gain-gainRight;
  double deltaVoltLeft=deltaGainLeft/gainLeft/alpha;
  double deltaVoltRight=deltaGainRight/gainRight/alpha;
  newLeft=1+deltaVoltLeft;
  newRight=1+deltaVoltRight;
  errorLeft=gain/(alpha*gainLeft*gainLeft);
  errorRight=gain/(alpha*gainRight*gainRight);
}

TH1D* FtofHVMainFrame::getH1GeoMean(UInt_t paddle){
  char strPaddle[50];
  sprintf(strPaddle,"%u",paddle);
  string strKey("GeoMean_");
  strKey+=strPaddle;
  FtofHistogramManager *h=new FtofHistogramManager;
  TH1D* h1=h->getH1(strKey);
  if(h1!=NULL) return h1;
  //  int panel=paddle/100%10;
  string strTitle("Geometric Mean of ");
  h1=h->addH1(strKey,strTitle+strPaddle,mPar[2],mPar[0],mPar[1]);
  return h1;
}
TH1D* FtofHVMainFrame::getH1LogRatio(UInt_t paddle){
  char strPaddle[50];
  sprintf(strPaddle,"%u",paddle);
  string strKey("LogRatio_");
  strKey+=strPaddle;
  FtofHistogramManager *h=new FtofHistogramManager;
  TH1D* h1=h->getH1(strKey);
  if(h1!=NULL) return h1;
  //int panel=paddle/100%10;
  string strTitle("Logrithm Ratio of ");
  h1=h->addH1(strKey,strTitle+strPaddle,50,-4.0,4.0);
  return h1;
}
void FtofHVMainFrame::ViewVolt(){
  FtofHistogramManager hm;
  fECanvas->GetCanvas()->Clear();
  fECanvas->GetCanvas()->Divide(3,2,0.005,0.01,18);
  for(int i=0;i<6;i++){
    fECanvas->GetCanvas()->cd(i+1);
    string name="guiHV_New_HV_Left_";
    name+=i+1+'0';
    hm.h1(name)->SetLineColor(kRed);
    hm.h1(name)->Draw();
    name="guiHV_New_HV_Right_";
    name+=i+1+'0';
    hm.h1(name)->SetLineColor(kBlue);
    hm.h1(name)->Draw("same");
  }
  fECanvas->GetCanvas()->Update();
}
void FtofHVMainFrame::FitEvent(vector<FtofSingleBar> mListBar[6][90]){
  FtofHistogramManager h;
  TH1D* h1Peak=h.addH1("guiHV_Minimizing_Peak","Minimizing peak of each bar",600,0.5,600.5);
  TH1D* h1PeakChi2=h.addH1("guiHV_Minimizing_Peak_chi2","Reduced chi2 of fitting minimizing peak",600,0.5,600.5);
  TH1D* h1PeakLogRatio=h.addH1("guiHV_Log_Ratio","Peak of log ratio for each bar",600,0.5,600.5);
  TH1D* h1LogChi2=h.addH1("guiHV_Log_Ratio_chi2","Reduced chi2 of fitting Log Ratio",600,0.5,600.5);
  TH1D* h1MipLeft=h.addH1("guiHV_Mip_Left","Minimum ionizing peak position on left side",600,0.5,600.5);
  TH1D* h1MipRight=h.addH1("guiHV_Mip_Right","Minimum ionizing peak position on right side",600,0.5,600.5);
  if(mFileOutput!=NULL){
    h1Peak->SetDirectory(mFileOutput);
    h1PeakChi2->SetDirectory(mFileOutput);
    h1PeakLogRatio->SetDirectory(mFileOutput);
    h1LogChi2->SetDirectory(mFileOutput);
    h1MipLeft->SetDirectory(mFileOutput);
    h1MipRight->SetDirectory(mFileOutput);
  }

  TH1D* h1HvLeft[6],*h1HvRight[6];
  for(int i=0;i<6;i++){  
    string name="guiHV_New_HV_Left_";
    name+=i+1+'0';
    string title="New voltage to old voltage on left side in sector ";
    title+=i+1+'0';
    h1HvLeft[i]=h.addH1(name,title,90,0.5,90.5);
    if(mFileOutput!=NULL)
      h1HvLeft[i]->SetDirectory(mFileOutput);

    name="guiHV_New_HV_Right_";
    name+=i+1+'0';
    title="New voltage to old voltage on right side in sector ";
    title+=i+1+'0';
    h1HvRight[i]=h.addH1(name,title,90,0.5,90.5);
    if(mFileOutput!=NULL){
      h1HvLeft[i]->SetDirectory(mFileOutput);
      h1HvRight[i]->SetDirectory(mFileOutput);
    }
  }
  fECanvas->GetCanvas()->cd(1);
  h1Peak->Draw();
  fECanvas->GetCanvas()->cd(2);
  h1PeakChi2->Draw();
  fECanvas->GetCanvas()->cd(3);
  h1PeakLogRatio->Draw();
  fECanvas->GetCanvas()->cd(4);
  h1MipLeft->Draw();
  fECanvas->GetCanvas()->cd(5);
  h1MipRight->Draw();
  fECanvas->GetCanvas()->cd(6);

  double peakIonizing,peakError,chi2Reduced;
  double centroid,centroidError;

  for(int iSector=0;iSector<6;iSector++){
    mNSector=iSector+1;
    for(UInt_t iPaddle=0;iPaddle<mNPanel1A+mNPanel1B+mNPanel2;iPaddle++){
      mNPaddle=iPaddle+1;
      UInt_t indexPaddle=(iSector+1)*1000+iPaddle;
      if(mListBar[iSector][iPaddle].size()>1){
        TH1D* h1GeoMean=getH1GeoMean(indexPaddle);
        TH1D* h1LogRatio=getH1LogRatio(indexPaddle);
	if(mFileOutput!=NULL){
	  h1GeoMean->SetDirectory(mFileOutput);
	  h1LogRatio->SetDirectory(mFileOutput);
	}
        FtofGeometricMean geoMean;
	geoMean.configure(&mPar[3]);
        geoMean.execute(mListBar[iSector][iPaddle],h1GeoMean,peakIonizing,peakError,chi2Reduced);
        h1Peak->SetBinContent(iSector*100+iPaddle+1,peakIonizing);
        h1Peak->SetBinError(iSector*100+iPaddle+1,peakError);
        h1PeakChi2->SetBinContent(iSector*100+iPaddle+1,chi2Reduced);
        h1PeakChi2->SetBinError(iSector*100+iPaddle+1,0);
        FtofLogRatio logRatio;
        logRatio.execute(mListBar[iSector][iPaddle],h1LogRatio,centroid,centroidError,chi2Reduced);
        h1PeakLogRatio->SetBinContent(iSector*100+iPaddle+1,centroid);
        h1PeakLogRatio->SetBinError(iSector*100+iPaddle+1,centroidError);
        h1LogChi2->SetBinContent(iSector*100+iPaddle+1,chi2Reduced);
        h1LogChi2->SetBinError(iSector*100+iPaddle+1,0);
        double newLeft,newRight,errorLeft,errorRight;
        calculateNewHV(peakIonizing,centroid,newLeft,newRight,errorLeft,errorRight,0);
        h1HvLeft[iSector]->SetBinContent(iPaddle+1,newLeft);
        h1HvLeft[iSector]->SetBinError(iPaddle+1,errorLeft);
        h1HvRight[iSector]->SetBinContent(iPaddle+1,newRight);
        h1HvRight[iSector]->SetBinError(iPaddle+1,errorRight);

        h1MipLeft->SetBinContent(iSector*100+iPaddle+1,peakIonizing/sqrt(exp(centroid)));
        h1MipRight->SetBinContent(iSector*100+iPaddle+1,peakIonizing*sqrt(exp(centroid)));
	UpdateStatusBar();
	UpdateStatistics(mListBar);
      }
    }
  }
}
void FtofHVMainFrame::CollectEvent(vector<FtofSingleBar> mListBar[6][90]){
  FtofCalibration theCalibration;
  if(mIsFilelist){
    //    int nFinishedPaddle=0;
    for(int iFile=0,nFile=mListFilename.size();iFile<nFile;iFile++){
      FtofRawEvio file(mListFilename[iFile].c_str());
      file.openFile();
      mFileCurrent=iFile+1;
      while(file.readNext() && mFinishedPaddle<mMaxPaddle){
	vector< pair<UInt_t,vector<FtofSingleBar> > > hits=file.hits();
	int nHits=hits.size();
	for(int iHit=0;iHit<nHits;iHit++){
	  UInt_t indexPaddle=hits[iHit].first;
	  UInt_t myPanel=indexPaddle/100%10;
	  UInt_t myPaddle=indexPaddle%100;
	  UInt_t mySector=indexPaddle/1000-1;
	  bool doProcess=true;
	  if(myPanel==0){//1A
	    UInt_t mySize=mListBar[mySector][myPaddle].size();
	    if(mySize>=mMaxEventPerPaddle){
	      doProcess=false;
	    }
	  }
	  else if(myPanel==1){//1B
	    UInt_t mySize=mListBar[mySector][myPaddle+mNPanel1A].size();
	    if(mySize>=mMaxEventPerPaddle){
	      doProcess=false;
	    }
	  }
	  else if(myPanel==2){//2
	    UInt_t mySize=mListBar[mySector][myPaddle+mNPanel1A+mNPanel1B].size();
	    if(mySize>=mMaxEventPerPaddle){
	      doProcess=false;
	    }
	  }
	  if(doProcess){
	    int nEntry=hits[iHit].second.size();
	    for(int iEntry=0;iEntry<nEntry;iEntry++){
	      //	      float adcLeft=hits[iHit].second[iEntry].adc().left();
	      //	      float adcRight=hits[iHit].second[iEntry].adc().right();
	      switch(myPanel){
	      case 0://1A
		if(theCalibration.goodAdc(mySector,myPaddle,true) && theCalibration.goodAdc(mySector,myPaddle,false))
		  mListBar[mySector][myPaddle].push_back(hits[iHit].second[iEntry]);
		break;
	      case 1://1B
		if(theCalibration.goodAdc(mySector,myPaddle,true) && theCalibration.goodAdc(mySector,myPaddle,false))
		  mListBar[mySector][myPaddle+mNPanel1A].push_back(hits[iHit].second[iEntry]);        
		break;
	      case 2://2
		if(theCalibration.goodAdc(mySector,myPaddle,true) && theCalibration.goodAdc(mySector,myPaddle,false))
		  mListBar[mySector][myPaddle+mNPanel1A+mNPanel1B].push_back(hits[iHit].second[iEntry]);        
		break;
	      }
	    }
	  }// end of doProcess
	}//loop over hits

	if(mNEvent%10000==0){
	  UpdateStatusBar();
	  UpdateStatistics(mListBar);
	}
	mNEvent++;
      }
      file.closeFile();
    }
  }
  else{
    TFile* fileRaw=new TFile(mNameFilelist.Data());
    TTree* tree=(TTree*)fileRaw->Get("ftofRawEvio");
    FtofRawEvioRoot* myRoot=new FtofRawEvioRoot(tree);
    if (myRoot->fChain != 0){
      Long64_t nentries = myRoot->fChain->GetEntriesFast();
      Long64_t nbytes = 0, nb = 0;
      mMaxEvent=nentries;
      for (Long64_t jentry=0; jentry<nentries;jentry++) {
        Long64_t ientry = myRoot->LoadTree(jentry);
        if (ientry < 0) break;
        nb = myRoot->fChain->GetEntry(jentry);
        int indexPaddle=myRoot->indexPaddle;
        int myPaddle=indexPaddle%100;
        int mySector=indexPaddle/100-1;
        FtofSingleBar myBar(myRoot->adcLeft,myRoot->adcRight,myRoot->tdcLeft,myRoot->tdcRight);
	if(theCalibration.goodAdc(mySector,myPaddle,true) && theCalibration.goodAdc(mySector,myPaddle,false))
	  mListBar[mySector][myPaddle].push_back(myBar);
        
        nbytes += nb;
        mNEvent++;
        if(mNEvent%100000==0){
          UpdateStatusBar();
          UpdateStatistics(mListBar);
        }

      }
    }

  }
  UpdateStatusBar();
  UpdateStatistics(mListBar);

}
void FtofHVMainFrame::DoCalibration(){
  vector<FtofSingleBar> mListBar[6][90];
  if(mFileOutput==NULL){
    mFileOutput=new TFile(mNameOutput.Data(),"RECREATE");
  }
  
  mStatus=1;
  BuildFilelist();
  mFileOutput->cd();

  UpdateStatusBar();

  CollectEvent(mListBar);  
  mStatus=2;
  FitEvent(mListBar);
  mStatus=100;
  ViewVolt();
  UpdateStatusBar();
  mFileOutput->Write();
}
void FtofHVMainFrame::HandleMenu(Int_t id){
  switch(id){
  case M_FILE_EXIT:
    CloseWindow();
    break;
  case M_CONFIG_1A:
    ReadHV(0);
    break;
  case M_CONFIG_1B:
    ReadHV(1);
    break;
  case M_CONFIG_2:
    ReadHV(2);
    break;

  case M_CONFIG_PAR:
    {
      new FtofHVConfigPar(fClient->GetRoot(), this,mPar);
    break;
    }
  case M_FILE_GO:
    DoNew();
    //    DoCalibration();
    break;
  case M_FILE_LOAD:
    DoLoad();
    break;
  case M_VIEW_STAT:
    ViewStat();
    break;
  case M_VIEW_VOLT:
    ViewVolt();
    break;
  case M_VIEW_FITS1:
    ViewFits(1);
    break;
  case M_VIEW_FITS2:
    ViewFits(2);
    break;
  case M_VIEW_FITS3:
    ViewFits(3);
    break;
  case M_VIEW_FITS4:
    ViewFits(4);
    break;
  case M_VIEW_FITS5:
    ViewFits(5);
    break;
  case M_VIEW_FITS6:
    ViewFits(6);
    break;
  case M_HELP_ABOUT:
    DoAbout();
  default:
    break;
  }
}
void FtofHVMainFrame::ViewFits(UInt_t sector){
  FtofHistogramManager hm;
  int iPad=0;
  int iCanvas=0;
  TCanvas* c1;
  TH1D* h1;
  UInt_t nTotal=mNPanel1A+mNPanel1B+mNPanel2;
  for(UInt_t iPaddle=0;iPaddle<nTotal;iPaddle++){
    string name="GeoMean_";
    name+=sector+'0';
    name+='0';
    name+=iPaddle/10+'0';
    name+=iPaddle%10+'0';
    h1=hm.h1(name);
    if(h1!=NULL){
      string title="S";
      title+=sector+'0';
      if(iPaddle<mNPanel1A){
	title+=" P1A ";
	if(iPaddle>9)
	  title+=iPaddle/10+'0';
	title+=iPaddle%10+'0';
      }
      else if(iPaddle<mNPanel1A+mNPanel1B){
	title+=" P1B ";
	if(iPaddle>9+mNPanel1A)
	  title+=(iPaddle-mNPanel1A)/10+'0';
	title+=(iPaddle-mNPanel1A)%10+'0';
      }
      else{
	title+=" P2 ";
	title+=(iPaddle-mNPanel1A-mNPanel1B)%10+'0';
      }
      h1->SetTitle("");


      iPad++;
      if(iPad==1){
	iCanvas++;
	string nameCanvas="canvas_";
	nameCanvas+=sector+'0';
	nameCanvas+='_';
	nameCanvas+=iCanvas+'0';
	c1=new TCanvas(nameCanvas.c_str(),nameCanvas.c_str(),800,600);
	c1->Divide(4,5);
      }
      c1->cd(iPad);
      h1->Draw();
      TPaveLabel *p1 = new TPaveLabel(.5,.75 , (.5+.40),(.75+.2) ,title.c_str(), "NDC");
      p1->SetFillColor(0);
      p1->SetTextFont(22);
      p1->SetTextColor(kBlack);
      p1->Draw();
 
      iPad=iPad%20;
    }
  }
}
void FtofHVMainFrame::UpdateStatusBar(){

  switch(mStatus){
  case 0:
    mStatusFilelist="list of files:";
    mStatusFilelist+=mNameFilelist;
    mStatusCurrent="Current file:";
    mStatusCurrent+=mFileCurrent;
    mStatusCurrent+="/";
    mStatusCurrent+=mFileTotal;
    mStatusTotal="Total events:";
    mStatusTotal+=mNEvent;
    mStatusTotal+="/";
    mStatusTotal+=mMaxEvent;
    mStatusStatus="Idle";
    break;
  case 1:
    mStatusFilelist="list of files:";
    mStatusFilelist+=mNameFilelist;
    mStatusCurrent="Current file:";
    mStatusCurrent+=mFileCurrent;
    mStatusCurrent+="/";
    mStatusCurrent+=mFileTotal;
    mStatusTotal="Total events:";
    mStatusTotal+=mNEvent;
    mStatusTotal+="/";
    mStatusTotal+=mMaxEvent;
    mStatusStatus="Reading events";
    break;
  case 2:
    mStatusFilelist="list of files:";
    mStatusFilelist+=mNameFilelist;
    mStatusCurrent="Sector:";
    mStatusCurrent+=mNSector;
    mStatusCurrent+="/6";
    mStatusTotal="Paddle:";
    mStatusTotal+=mNPaddle;
    mStatusTotal+="/90";
    mStatusStatus="Fitting events";
    break;
  case 3:
    mStatusFilelist="";
    mStatusCurrent="";
    mStatusTotal="";
    mStatusStatus="Load Root histograms";
    break;
  case 10:
    mStatusFilelist="list of files:";
    mStatusFilelist+=mNameFilelist;
    mStatusCurrent="Current file:";
    mStatusCurrent+=mFileCurrent;
    mStatusCurrent+="/";
    mStatusCurrent+=mFileTotal;
    mStatusTotal="Total events:";
    mStatusTotal+=mNEvent;
    mStatusTotal+="/";
    mStatusTotal+=mMaxEvent;
    mStatusStatus="Reading HV for 1A";
    break;
  case 11:
    mStatusFilelist="list of files:";
    mStatusFilelist+=mNameFilelist;
    mStatusCurrent="Current file:";
    mStatusCurrent+=mFileCurrent;
    mStatusCurrent+="/";
    mStatusCurrent+=mFileTotal;
    mStatusTotal="Total events:";
    mStatusTotal+=mNEvent;
    mStatusTotal+="/";
    mStatusTotal+=mMaxEvent;
    mStatusStatus="Reading HV for 1B";
    break;
  case 12:
    mStatusFilelist="list of files:";
    mStatusFilelist+=mNameFilelist;
    mStatusCurrent="Current file:";
    mStatusCurrent+=mFileCurrent;
    mStatusCurrent+="/";
    mStatusCurrent+=mFileTotal;
    mStatusTotal="Total events:";
    mStatusTotal+=mNEvent;
    mStatusTotal+="/";
    mStatusTotal+=mMaxEvent;
    mStatusStatus="Reading HV for Panel 2";
    break;
  case 100:
    mStatusStatus="Done";
    mStatusFilelist="";
    mStatusCurrent="";
    mStatusTotal="";
    break;
  }
  
  if(mStatus<0){
    mStatusStatus="Error:";
    mStatusStatus+=mStatus;
  }

  fStatusBar->SetText(mStatusFilelist,0);
  fStatusBar->SetText(mStatusCurrent,1);
  fStatusBar->SetText(mStatusTotal,2);
  fStatusBar->SetText(mStatusStatus,3);
  gClient->ProcessEventsFor(fStatusBar);

}
FtofHVMainFrame::FtofHVMainFrame(const TGWindow *p,UInt_t w, UInt_t h):TGMainFrame(p,w,h),mPar{100,15000,745,50000,5,500,100},mHVOld{0},mHVNew{0}{
  cout<<"constructor"<<endl;
  mNEvent=0;
  mMaxEvent=100000000;
  mMaxEventPerPaddle=100000;
  mNPanel1A=23;
  mNPanel1B=62;
  mNPanel2=5;
  mMaxPaddle=(mNPanel1A+mNPanel1B+mNPanel2)*6;
  mNameFilelist="filelist";
  mFileCurrent=0;
  mFileTotal=0;
  mStatus=0;
  mFinishedPaddle=0;
  mNameOutput="ftofHighVoltage.root";
  mFileOutput=NULL;
  //  fMain=new TGMainFrame(p,w,h);
  //  this->Connect("CloseWindow()", "FtofHVMainFrame", this, "CloseWindow()");
  fMenuBar=new TGMenuBar(this,50,25,kHorizontalFrame);
  fMenuFile=new TGPopupMenu(gClient->GetDefaultRoot());
  fMenuFile->AddEntry("&New",M_FILE_GO);
  fMenuFile->AddEntry("&Load",M_FILE_LOAD);
  fMenuFile->AddEntry("E&xit",M_FILE_EXIT);
  fMenuFile->Connect("Activated(Int_t)","FtofHVMainFrame",this,"HandleMenu(Int_t)");
  fMenuConfig=new TGPopupMenu(gClient->GetDefaultRoot());
  fMenuConfig->AddEntry("Open HV files of panel 1&A",M_CONFIG_1A);
  fMenuConfig->AddEntry("Open HV files of panel 1&B",M_CONFIG_1B);
  fMenuConfig->AddEntry("Open HV files of panel &2",M_CONFIG_2);
  fMenuConfig->AddEntry("&Under the Hood",M_CONFIG_PAR);
  fMenuConfig->Connect("Activated(Int_t)","FtofHVMainFrame",this,"HandleMenu(Int_t)");
  fMenuView=new TGPopupMenu(gClient->GetDefaultRoot());
  fMenuView->AddEntry("View &statitics",M_VIEW_STAT);
  fMenuView->AddEntry("View &voltage",M_VIEW_VOLT);
  fMenuView->AddEntry("View sector &1",M_VIEW_FITS1);
  fMenuView->AddEntry("View sector &2",M_VIEW_FITS2);
  fMenuView->AddEntry("View sector &3",M_VIEW_FITS3);
  fMenuView->AddEntry("View sector &4",M_VIEW_FITS4);
  fMenuView->AddEntry("View sector &5",M_VIEW_FITS5);
  fMenuView->AddEntry("View sector &6",M_VIEW_FITS6);
  fMenuView->Connect("Activated(Int_t)","FtofHVMainFrame",this,"HandleMenu(Int_t)");
  fMenuHelp=new TGPopupMenu(gClient->GetDefaultRoot());
  fMenuHelp->AddEntry("&About",M_HELP_ABOUT);
  fMenuHelp->Connect("Activated(Int_t)","FtofHVMainFrame",this,"HandleMenu(Int_t)");

  fMenuBar->AddPopup("&Action",fMenuFile,new TGLayoutHints(kLHintsTop|kLHintsLeft,0, 4, 0, 0));
  fMenuBar->AddPopup("&Configure",fMenuConfig,new TGLayoutHints(kLHintsTop|kLHintsLeft,0, 4, 0, 0));
  fMenuBar->AddPopup("&View",fMenuView,new TGLayoutHints(kLHintsTop|kLHintsLeft,0, 4, 0, 0));
  fMenuBar->AddPopup("&Help",fMenuHelp,new TGLayoutHints(kLHintsTop|kLHintsLeft,0, 4, 0, 0));

  this->AddFrame(fMenuBar,new TGLayoutHints(kLHintsTop|kLHintsExpandX,2,2,2,5));

  fECanvas=new TRootEmbeddedCanvas("ECanvas",this,w-100,h-50);
  fECanvas->GetCanvas()->Divide(3,2,0.005,0.01,18);
  this->AddFrame(fECanvas, new TGLayoutHints(kLHintsExpandX| kLHintsExpandY,10,10,10,1));


  Int_t parts[]={20,30,30,20};
  fStatusBar=new TGStatusBar(this,50,25,kHorizontalFrame);
  fStatusBar->SetParts(parts,4);
  this->AddFrame(fStatusBar,new TGLayoutHints(kLHintsBottom|kLHintsLeft|kLHintsExpandX,0,0,2,2));


  this->SetWindowName("FTOFHighVoltage - CLAS12 FTOF High Voltage Calibration");
  // Map all subwindows of main frame
  this->MapSubwindows();
  // Initialize the layout algorithm
  this->Resize(this->GetDefaultSize());
  // Map main frame
  this->MapWindow();


  UpdateStatusBar();

}
FtofHVMainFrame::~FtofHVMainFrame() {
  cout<<"destructor"<<endl;
  // Clean up used widgets: frames, buttons, layouthints
  this->Cleanup();
}
void FtofHVMainFrame::BuildFilelist(){
  UpdateStatusBar();
  ifstream inFile(mNameFilelist.Data());
  
  char buff[1024];
  while(inFile.good()){
    inFile.getline(buff,1024);
    string myName(buff);
    if(myName.length()!=0){
      mListFilename.push_back(myName);
    }
  }
  mFileTotal=mListFilename.size();
}
void FtofHVMainFrame::OutputHV(){
  FtofHistogramManager hm;

  FtofHistogramManager h;
  //TH1D* h1Peak=h.getH1("guiHV_Minimizing_Peak");

  ofstream out1A("hv1a_out.txt");
  ofstream out1B("hv1b_out.txt");
  ofstream out2("hv2_out.txt");
  for(int iSector=0;iSector<6;iSector++){
    string name="guiHV_New_HV_Left_";
    name+=iSector+1+'0';
    TH1D* h1Left=hm.h1(name);
    name="guiHV_New_HV_Right_";
    name+=iSector+1+'0';
    TH1D* h1Right=hm.h1(name);
    if(!h1Left || !h1Right) return;
    for(UInt_t iPaddle=0;iPaddle<mNPanel1A;iPaddle++){//1A
      if(mHVOld[iSector][iPaddle][0]<-1 && mHVOld[iSector][iPaddle][1]<-1){
	string strIndex;
	strIndex+=iSector+1+'0';
	strIndex+=".";
	strIndex+=(iPaddle+1)/10+'0';
	strIndex+=(iPaddle+1)%10+'0';
	mHVNew[iSector][iPaddle][0]=mHVOld[iSector][iPaddle][0]*h1Left->GetBinContent(iPaddle+1);
	mHVNew[iSector][iPaddle][1]=mHVOld[iSector][iPaddle][1]*h1Right->GetBinContent(iPaddle+1);
	out1A<<strIndex.c_str()<<"\t"<<mHVNew[iSector][iPaddle][0]<<"\t"<<mHVNew[iSector][iPaddle][1]<<endl;
      }
    }
    for(UInt_t iPaddle=0;iPaddle<mNPanel1B;iPaddle++){//1B
      UInt_t indexPaddle=iPaddle+mNPanel1A;
      if(mHVOld[iSector][indexPaddle][0]<-1 && mHVOld[iSector][indexPaddle][1]<-1){
	string strIndex;
	strIndex+=iSector+1+'0';
	strIndex+=".";
	strIndex+=(iPaddle+1)/10+'0';
	strIndex+=(iPaddle+1)%10+'0';
	mHVNew[iSector][indexPaddle][0]=mHVOld[iSector][indexPaddle][0]*h1Left->GetBinContent(indexPaddle+1);
	mHVNew[iSector][indexPaddle][1]=mHVOld[iSector][indexPaddle][1]*h1Right->GetBinContent(indexPaddle+1);
	out1B<<strIndex.c_str()<<"\t"<<mHVNew[iSector][indexPaddle][0]<<"\t"<<mHVNew[iSector][indexPaddle][1]<<endl;
      }
    }
    for(UInt_t iPaddle=0;iPaddle<mNPanel2;iPaddle++){//2
      UInt_t indexPaddle=iPaddle+mNPanel1A+mNPanel1B;
      if(mHVOld[iSector][indexPaddle][0]<-1 && mHVOld[iSector][indexPaddle][1]<-1){
	string strIndex;
	strIndex+=iSector+1+'0';
	strIndex+=".";
	strIndex+=(iPaddle+1)/10+'0';
	strIndex+=(iPaddle+1)%10+'0';
	mHVNew[iSector][indexPaddle][0]=mHVOld[iSector][indexPaddle][0]*h1Left->GetBinContent(indexPaddle+1);
	mHVNew[iSector][indexPaddle][1]=mHVOld[iSector][indexPaddle][1]*h1Right->GetBinContent(indexPaddle+1);
	out2<<strIndex.c_str()<<"\t"<<mHVNew[iSector][indexPaddle][0]<<"\t"<<mHVNew[iSector][indexPaddle][1]<<endl;
      }
    }
  }
  out1A.close();
  out1B.close();
  out2.close();
}
void FtofHVMainFrame::CloseWindow(){
  OutputHV();
  if(mFileOutput!=NULL)
    mFileOutput->Close();
  DeleteWindow();
  gApplication->Terminate();
}
void FtofHVMainFrame::UpdateStatistics(vector<FtofSingleBar> mListBar[6][90]){
  gStyle->SetOptStat(0);
  
  FtofHistogramManager *hm=new FtofHistogramManager;
  switch(mStatus){
  case 0:
    break;
  case 1:
    for(int iSector=0;iSector<6;iSector++){
      string nameSector;
      nameSector+=iSector+1+'0';
      string nameHist="stat_sector";
      nameHist+=nameSector;
      string nameTitle="statistics of paddles in sector ";
      nameTitle+=nameSector;
      hm->addH1(nameHist,nameTitle,90,0.5,90.5);
      TH1D* h1=hm->h1(nameHist);
      if(mFileOutput!=NULL)
        h1->SetDirectory(mFileOutput);

      mFinishedPaddle=0;
      for(UInt_t iPaddle=0;iPaddle<90;iPaddle++){
	UInt_t mySize=mListBar[iSector][iPaddle].size();
	if(mySize>=mMaxEventPerPaddle){
	  mFinishedPaddle++;
	}
	h1->SetBinContent(iPaddle+1,mySize);
      }
      fECanvas->GetCanvas()->cd(iSector+1);
      h1->Draw();
    }
    fECanvas->GetCanvas()->Modified();
    fECanvas->GetCanvas()->Update();
    break;
  case 2:
    for(int i=0;i<6;i++){
      fECanvas->GetCanvas()->GetPad(i+1)->Modified();
    }
    fECanvas->GetCanvas()->Update();
    break;
  }
  delete(hm);
}
void FtofHVMainFrame::ViewStat(){
  gStyle->SetOptStat(0);
  fECanvas->GetCanvas()->Clear();
  fECanvas->GetCanvas()->Divide(3,2,0.005,0.01,18);
  FtofHistogramManager *hm=new FtofHistogramManager;
  for(int iSector=0;iSector<6;iSector++){
    string nameSector;
    nameSector+=iSector+1+'0';
    string nameHist="stat_sector";
    nameHist+=nameSector;
    string nameTitle="statistics of paddles in sector ";
    nameTitle+=nameSector;
    hm->addH1(nameHist,nameTitle,90,0.5,90.5);
    TH1D* h1=hm->h1(nameHist);
    if(mFileOutput!=NULL)
      h1->SetDirectory(mFileOutput);

    fECanvas->GetCanvas()->cd(iSector+1);
    h1->Draw();
  }
  fECanvas->GetCanvas()->Modified();
  fECanvas->GetCanvas()->Update();
  delete(hm);
}
