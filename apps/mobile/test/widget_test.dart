import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:pch_mobile/main.dart';
import 'package:pch_mobile/providers/providers.dart';

void main() {
  testWidgets('세션이 없으면 로그인 화면을 표시한다', (tester) async {
    TestWidgetsFlutterBinding.ensureInitialized();
    SharedPreferences.setMockInitialValues({});
    bindPchSharedPreferences(await SharedPreferences.getInstance());
    await tester.pumpWidget(
      const ProviderScope(
        child: PchApp(),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.textContaining('로그인'), findsWidgets);
  });
}
