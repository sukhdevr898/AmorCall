import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';

enum DuoraTab {
  home,
  chat,
  call,
  notes,
  settings,
}

class DuoraBottomBar extends StatelessWidget {
  final DuoraTab currentTab;
  final int unreadChatCount;
  final ValueChanged<DuoraTab> onTabSelected;

  const DuoraBottomBar({
    super.key,
    required this.currentTab,
    this.unreadChatCount = 0,
    required this.onTabSelected,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: DuoraColors.surface.withOpacity(0.95),
        border: const Border(
          top: BorderSide(color: DuoraColors.borderMuted, width: 1),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.4),
            blurRadius: 20,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _NavItem(
                tab: DuoraTab.home,
                label: 'Home',
                icon: Icons.home_rounded,
                isSelected: currentTab == DuoraTab.home,
                onTap: () => onTabSelected(DuoraTab.home),
              ),
              _NavItem(
                tab: DuoraTab.chat,
                label: 'Chat',
                icon: Icons.chat_bubble_rounded,
                isSelected: currentTab == DuoraTab.chat,
                badgeCount: unreadChatCount,
                onTap: () => onTabSelected(DuoraTab.chat),
              ),
              _NavItem(
                tab: DuoraTab.call,
                label: 'Calls',
                icon: Icons.call_rounded,
                isSelected: currentTab == DuoraTab.call,
                onTap: () => onTabSelected(DuoraTab.call),
              ),
              _NavItem(
                tab: DuoraTab.notes,
                label: 'Notes',
                icon: Icons.edit_note_rounded,
                isSelected: currentTab == DuoraTab.notes,
                onTap: () => onTabSelected(DuoraTab.notes),
              ),
              _NavItem(
                tab: DuoraTab.settings,
                label: 'Settings',
                icon: Icons.settings_rounded,
                isSelected: currentTab == DuoraTab.settings,
                onTap: () => onTabSelected(DuoraTab.settings),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  final DuoraTab tab;
  final String label;
  final IconData icon;
  final bool isSelected;
  final int badgeCount;
  final VoidCallback onTap;

  const _NavItem({
    required this.tab,
    required this.label,
    required this.icon,
    required this.isSelected,
    this.badgeCount = 0,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final activeColor = DuoraColors.primaryPink;
    final inactiveColor = DuoraColors.textMuted;

    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Stack(
              clipBehavior: Clip.none,
              children: [
                AnimatedScale(
                  scale: isSelected ? 1.12 : 1.0,
                  duration: const Duration(milliseconds: 200),
                  child: Icon(
                    icon,
                    size: 24,
                    color: isSelected ? activeColor : inactiveColor,
                  ),
                ),
                if (badgeCount > 0)
                  Positioned(
                    top: -4,
                    right: -8,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                      decoration: BoxDecoration(
                        color: DuoraColors.primaryPink,
                        shape: BoxShape.circle,
                        border: Border.all(color: DuoraColors.surface, width: 1.5),
                      ),
                      child: Text(
                        badgeCount > 99 ? '99+' : badgeCount.toString(),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 9,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected ? activeColor : inactiveColor,
              ),
            ),
            const SizedBox(height: 4),
            // Glowing bottom dot / pill indicator
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              width: isSelected ? 18 : 0,
              height: 3,
              decoration: BoxDecoration(
                color: isSelected ? DuoraColors.primaryPink : Colors.transparent,
                borderRadius: BorderRadius.circular(2),
                boxShadow: isSelected
                    ? [
                        BoxShadow(
                          color: DuoraColors.primaryPink.withOpacity(0.6),
                          blurRadius: 6,
                          spreadRadius: 1,
                        ),
                      ]
                    : null,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
