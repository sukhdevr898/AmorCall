import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';
import '../home/home_tab.dart';
import '../chat/chat_tab.dart';
import '../call/call_tab.dart';
import '../notes/notes_tab.dart';
import '../settings/settings_tab.dart';
import 'duora_bottom_bar.dart';

class MainShellScreen extends StatefulWidget {
  final DuoraTab initialTab;
  const MainShellScreen({super.key, this.initialTab = DuoraTab.home});

  @override
  State<MainShellScreen> createState() => _MainShellScreenState();
}

class _MainShellScreenState extends State<MainShellScreen> {
  late DuoraTab _currentTab;

  @override
  void initState() {
    super.initState();
    _currentTab = widget.initialTab;
  }

  void _onTabSelected(DuoraTab tab) {
    setState(() => _currentTab = tab);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: DuoraColors.background,
      body: IndexedStack(
        index: _currentTab.index,
        children: [
          HomeTab(onNavigateTab: _onTabSelected),
          const ChatTab(),
          const CallTab(),
          const NotesTab(),
          const SettingsTab(),
        ],
      ),
      bottomNavigationBar: DuoraBottomBar(
        currentTab: _currentTab,
        unreadChatCount: 1,
        onTabSelected: _onTabSelected,
      ),
    );
  }
}
