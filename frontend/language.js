const LANGUAGES = {

  en: {

    // NAVIGATION
    navHome: "Home",
    navLogin: "Login",
    navResults: "Results",
    navAdmin: "Admin",

    // LOGIN
    voterLogin: "Voter Login",
    verifyIdentity: "Verify Identity & Send OTP",
    enterOtp: "Enter 6-digit OTP",
    verifyOtp: "Verify OTP",
    biometric: "Verify Biometric",
    proceedVote: "Proceed to Vote",

    // VOTING
    castVote: "Cast Vote",
    voteNow: "Vote Now",
    confirmVote: "Confirm Vote",
    noneOption: "None Of The Above",

    // RESULTS
    totalVotes: "Total Votes",
    registeredVoters: "Registered Voters",
    leadingCandidate: "Leading Candidate",
    recentVoters: "Recent Voters",

    // ALERTS
    invalidOtp: "Invalid OTP",
    alreadyVoted: "Already Voted",
    voteSuccess: "Vote Submitted Successfully",
    biometricSuccess: "Biometric Verified",
    livenessSuccess: "Liveness Verified",

    // ADMIN
    openVoting: "Open Voting",
    closeVoting: "Close Voting",
    approveVoter: "Approve Voter",
    pendingRequests: "Pending Requests",

    // COMMON
    loading: "Loading...",
    success: "Success",
    failed: "Failed",
    back: "Back"

  },

  bn: {

    navHome: "হোম",
    navLogin: "লগইন",
    navResults: "রেজাল্ট",
    navAdmin: "অ্যাডমিন",

    voterLogin: "ভোটার লগইন",
    verifyIdentity: "পরিচয় যাচাই ও OTP পাঠান",
    enterOtp: "৬ সংখ্যার OTP দিন",
    verifyOtp: "OTP যাচাই করুন",
    biometric: "বায়োমেট্রিক যাচাই",
    proceedVote: "ভোট দিতে এগিয়ে যান",

    castVote: "ভোট দিন",
    voteNow: "এখন ভোট দিন",
    confirmVote: "ভোট নিশ্চিত করুন",
    noneOption: "উপরের কেউ নয়",

    totalVotes: "মোট ভোট",
    registeredVoters: "নিবন্ধিত ভোটার",
    leadingCandidate: "এগিয়ে থাকা প্রার্থী",
    recentVoters: "সাম্প্রতিক ভোটার",

    invalidOtp: "ভুল OTP",
    alreadyVoted: "আগেই ভোট দিয়েছেন",
    voteSuccess: "সফলভাবে ভোট জমা হয়েছে",
    biometricSuccess: "বায়োমেট্রিক যাচাই সফল",
    livenessSuccess: "লাইভনেস যাচাই সফল",

    openVoting: "ভোট চালু করুন",
    closeVoting: "ভোট বন্ধ করুন",
    approveVoter: "ভোটার অনুমোদন",
    pendingRequests: "অপেক্ষমাণ অনুরোধ",

    loading: "লোড হচ্ছে...",
    success: "সফল",
    failed: "ব্যর্থ",
    back: "ফিরে যান"

  },

  hi: {

    navHome: "होम",
    navLogin: "लॉगिन",
    navResults: "रिजल्ट",
    navAdmin: "एडमिन",

    voterLogin: "मतदाता लॉगिन",
    verifyIdentity: "पहचान सत्यापित करें और OTP भेजें",
    enterOtp: "6 अंकों का OTP दर्ज करें",
    verifyOtp: "OTP सत्यापित करें",
    biometric: "बायोमेट्रिक सत्यापन",
    proceedVote: "वोट करने जाएं",

    castVote: "वोट दें",
    voteNow: "अभी वोट दें",
    confirmVote: "वोट पुष्टि करें",
    noneOption: "इनमें से कोई नहीं",

    totalVotes: "कुल वोट",
    registeredVoters: "पंजीकृत मतदाता",
    leadingCandidate: "आगे चल रहा उम्मीदवार",
    recentVoters: "हाल के मतदाता",

    invalidOtp: "अमान्य OTP",
    alreadyVoted: "पहले ही वोट दे चुके हैं",
    voteSuccess: "वोट सफलतापूर्वक जमा हुआ",
    biometricSuccess: "बायोमेट्रिक सत्यापित",
    livenessSuccess: "लाइवनेस सत्यापित",

    openVoting: "वोटिंग शुरू करें",
    closeVoting: "वोटिंग बंद करें",
    approveVoter: "मतदाता स्वीकृत करें",
    pendingRequests: "लंबित अनुरोध",

    loading: "लोड हो रहा है...",
    success: "सफल",
    failed: "विफल",
    back: "वापस"

  }

};

// CURRENT LANGUAGE
let currentLanguage =
  localStorage.getItem("language") || "en";

// APPLY TRANSLATION
function applyLanguage(lang) {

  currentLanguage = lang;

  localStorage.setItem(
    "language",
    lang
  );

  document
    .querySelectorAll("[data-i18n]")
    .forEach(el => {

      const key =
        el.getAttribute("data-i18n");

      if (
        LANGUAGES[lang] &&
        LANGUAGES[lang][key]
      ) {

        el.innerText =
          LANGUAGES[lang][key];
      }
    });
}

// INIT
document.addEventListener(
  "DOMContentLoaded",
  () => {

    applyLanguage(currentLanguage);

    const switcher =
      document.getElementById(
        "languageSwitcher"
      );

    if (switcher) {

      switcher.value =
        currentLanguage;

      switcher.addEventListener(
        "change",
        e => {

          applyLanguage(
            e.target.value
          );
        }
      );
    }
  }
);
